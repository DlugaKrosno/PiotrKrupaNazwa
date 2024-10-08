package com.example.piotrkrupa.booking;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Builder
@Getter
@Setter
public class Booking {

    String bookingid;
    String roomid;
    String firstname;
    String lastname;
    boolean depositpaid;
    BookingDates bookingdates;

}
