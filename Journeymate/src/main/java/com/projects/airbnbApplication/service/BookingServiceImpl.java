package com.projects.airbnbApplication.service;

import com.projects.airbnbApplication.dto.BookingDto;
import com.projects.airbnbApplication.dto.BookingRequest;
import com.projects.airbnbApplication.dto.GuestDto;
import com.projects.airbnbApplication.entity.*;
import com.projects.airbnbApplication.entity.enums.BookingStatus;
import com.projects.airbnbApplication.exception.ResourceNotFoundException;
import com.projects.airbnbApplication.exception.UnAuthorisedException;
import com.projects.airbnbApplication.repository.BookingRepository;
import com.projects.airbnbApplication.repository.GuestRepository;
import com.projects.airbnbApplication.repository.InventoryRepository;
import com.projects.airbnbApplication.repository.RoomRepository;
import com.projects.airbnbApplication.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.projects.airbnbApplication.util.UserUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final InventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;
    private final HotelService hotelRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final GuestRepository guestRepository;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public BookingDto initializeBooking(BookingRequest bookingRequest) {

        log.info("Initialising booking for hotel {} , room {} , date {} - {}",
                bookingRequest.getHotelId(), bookingRequest.getRoomId(),
                bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());

        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId());

        Room room =  roomRepository.findById(bookingRequest.getRoomId())
                        .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + bookingRequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
                room.getId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),
                bookingRequest.getRoomsCount()
        );

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate()) + 1;

        if(inventoryList.size() != daysCount) {
            throw new IllegalStateException("Room is not available for given data range");
        }

//        inventoryRepository.findAndReserveInventory(room.getId(), bookingRequest.getCheckInDate(),
//                bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        inventoryRepository.initializeBooking(
                room.getId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),
                bookingRequest.getRoomsCount()
        );

        BigDecimal price = pricingService.calculatePrice(inventoryList);
        BigDecimal finalPrice = price.multiply(BigDecimal.valueOf(inventoryList.size()));

        Booking booking = Booking.builder()
                .status(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .roomsCount(bookingRequest.getRoomsCount())
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .price(finalPrice)
                .build();

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info(" adding guests for the booking ID {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if(hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has expired");
        }

        if(booking.getStatus() !=  BookingStatus.RESERVED) {
            throw new IllegalStateException("Booking status is not under reserved state, cannot add guests");
        }

        for(GuestDto guestDto : guestDtoList) {
            Guest guest =  modelMapper.map(guestDto, Guest.class);
            guest.setUser(getCurrentUser());
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setStatus(BookingStatus.GUESTS_ADDED);
        booking =  bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    public String initializePayments(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("Booking not found with id: " + bookingId));

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())) {
            throw new BadCredentialsException("booking user and current user should be same");
        }

        if(hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking, frontendUrl+"/payments/success", frontendUrl+"/payments/failure");

        booking.setStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);

        return sessionUrl;
    }

    @Override
    public void capturePayment(Event event) {
        if("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

            if(session == null) return;

            String sessionId = session.getId();

            Booking booking = bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(() ->
                    new ResourceNotFoundException("Booking not found with session ID: " +  sessionId));

            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            log.info("successfully confirmed the booking for booking ID {}", booking.getId());
        }else {
            log.warn("unhandled event type {}", event.getType());
        }
    }

    @Override
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())) {
            throw new UnAuthorisedException("booking user and current user should be same");
        }

        if(booking.getStatus() !=  BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only bookings under confirmed state can be cancelled");
        }

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());

        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();
            Refund.create(refundParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BookingStatus getPaymentStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())) {
            throw new UnAuthorisedException("booking user and current user should be same");
        }

        return booking.getStatus();
    }

    @Override
    public List<BookingDto> getAllBookingsByHotelId(Long hotelId) throws AccessDeniedException {

        Hotel hotel = hotelRepository.findById(hotelId);

        User user = getCurrentUser();

        log.info("verifying wether the current user is the owner of the hotel");

        if(!user.equals(hotel.getOwner())) {
            throw new AccessDeniedException("booking user and current user should be same");
        }

        log.info("Getting all the bookings of the hotel {}", hotel.getId());

        List<Booking> bookings = bookingRepository.getAllBookingsByHotelId(hotel);

        return bookings.stream()
                .map((element) -> modelMapper.map(element, BookingDto.class))
                .toList();
    }

    @Override
    public List<BookingDto> getMyBookings() {
        User user = getCurrentUser();

        return bookingRepository.findByUser(user)
                .stream()
                .map((element) -> modelMapper.map(element, BookingDto.class))
                .toList();
    }


    public Boolean hasBookingExpired(Booking booking) {
        log.info("checking wether the booking ID {} has expired", booking.getId());

        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }
}
