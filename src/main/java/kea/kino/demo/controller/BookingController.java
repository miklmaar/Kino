package kea.kino.demo.controller;

import kea.kino.demo.model.Booking;
import kea.kino.demo.repository.BookingRepository;
import kea.kino.demo.repository.FilmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Book;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class BookingController
{
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    FilmRepository filmRepository;

    /* Create */
    /* Read */
    /* Read All */
    /* Update */


    /* Create */
    @PostMapping("/**1")
    public String createBooking(@ModelAttribute Booking newBooking,
                                @ModelAttribute Date date,
                                @ModelAttribute int hour,
                                @ModelAttribute int minute,

                                Model model)
    {
        Timestamp stamp = assembleTimeStamp(date, hour, minute);
        /* update timestamp */
        newBooking.setShowTime(stamp);
        Booking createdBooking = bookingRepository.save(newBooking);
        model.addAttribute("createdBooking", createdBooking); // saved booking brought along for verification;
        return "**1";
    }

    private Timestamp assembleTimeStamp(Date date, int hour, int minute)
    {

        int earlyYear = date.toLocalDate().getYear();
        int earlyMonth = date.toLocalDate().getMonthValue();
        int earlyDayOfMonth = date.toLocalDate().getDayOfMonth();

        return Timestamp.valueOf(LocalDateTime.of(earlyYear, earlyMonth, earlyDayOfMonth, hour, minute));
    }

    /* Read */
    @PostMapping("/**2")
    public String findBookingsByCustomerNameAndShowTime(@ModelAttribute String customerName,
                                                        @ModelAttribute Date earlyDate,
                                                        @ModelAttribute int earlyDateHour,
                                                        @ModelAttribute int earlyDateMinute,
                                                        @ModelAttribute Date laterDate,
                                                        @ModelAttribute int laterDateHour,
                                                        @ModelAttribute int laterDateMinute,
                                                        Model model)
    {
        /* idea: same controller method used whether user fills in name or not, we simply null-check and
         * search without customer name / dates (if dates are 'equal' - or earlyDate is 'later' than
         * laterDate, they are discarded)*/

        /* convert from incoming model data to java java.sql.TimeStamp, mappable directly to SQL TIMESTAMP */
        Timestamp earlyStamp = assembleTimeStamp(earlyDate, earlyDateHour, earlyDateMinute);
        Timestamp laterStamp = assembleTimeStamp(laterDate, laterDateHour, laterDateMinute);

        List<Booking> bookings = new ArrayList<>();

        /* if no name supplied, search only for dates */
        if(customerName.length() < 1)
        {
            bookings = bookingRepository.findBookingsByShowTimeBetween(earlyStamp, laterStamp);
        } else
        {
            bookings = bookingRepository.findBookingsByCustomerNameContainingAndShowTimeBetween(customerName,
                                                                                                earlyStamp, laterStamp);
        }
        return "**2";
    }


    @PostMapping("/editBooking")
    public String editBooking(@RequestParam int id,
                              Model model)
    {
        /* tænker at vi kan tilføje 'film' til modellen her, hvis vi vil have en dropdown
         * liste med film:*/
//        model.addAttribute("films",filmRepository.findFilmsByVisibleOnSiteTrue());

        model.addAttribute("booking", bookingRepository.findById(id).get());
        return "simple-edit-booking";
    }

    /* Update */
    @PostMapping("/updateBooking")
    public String saveEditedBooking(@RequestParam int id,
                                    @RequestParam int showRoom,
                                    @RequestParam String customerName,
                                    @RequestParam int numberOfSeats,
                                    @RequestParam boolean isPaid,
                                    @RequestParam int hour, /* image: in form, drop down 0-24*/
                                    @RequestParam int minute, /* image: in form, drop down 0-60 */
                                    @RequestParam Date date /* image: data field */
                                   )
    {
        Optional<Booking> possibleBookingbyId = bookingRepository.findById(id);/* fetching booking */
        if(possibleBookingbyId.isPresent())
        {
            Booking booking = possibleBookingbyId.get();
            booking.setShowRoom(showRoom);
            booking.setCustomerName(customerName);
            booking.setNumberOfSeats(numberOfSeats);
            booking.setPaid(isPaid);

            LocalDate lDate = date.toLocalDate(); /* converting split up date and timey data to timestamp for db*/
            Timestamp newShowtimeStamp = Timestamp.valueOf(LocalDateTime.of(lDate.getYear(),
                                                                            lDate.getMonth(),
                                                                            lDate.getDayOfMonth(),
                                                                            hour,
                                                                            minute));
            booking.setShowTime(newShowtimeStamp);
            bookingRepository.save(booking); /* saving booking back to db */
        }

        return "simple-calendar";
    }


    /* Gonna be written in the morn :) */

    /* Read All */
    @GetMapping("/calendar")
    public String displayAllBookings(Model model)
    {
        ArrayList<Booking> bookings = new ArrayList<Booking>();
        bookingRepository.findAll().forEach(bookings::add);

        bookings.forEach(System.out::println);

        for(Booking b : bookings) { b.getFilm().setBookings(new HashSet<>()); }

        model.addAttribute("bookings", bookings);
        return "calendar";
    }

    /* this is so far just for testing purposes */
    @GetMapping("/simple-calendar")
    public String displayAllBookings_SimpleCalendar(Model model)
    {
        ArrayList<Booking> bookings = new ArrayList<Booking>();
        bookingRepository.findAll().forEach(bookings::add);
        bookings.sort(Booking::compareTo);
//        bookings.sort((o1, o2) -> o1.compareTo(o2));  // sort med lambda
        model.addAttribute("bookings", bookings);
        return "simple-calendar";
    }
}
