package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.utils.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserBookingService {
    private User user;
    private List<User> userList;
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final String USER_PATH = "app/src/main/java/ticket/booking/localDb/users.json";

    public UserBookingService(User user1) throws IOException {
        this.user = user1;
        loadUsers();
    }

    public UserBookingService() throws IOException {
        loadUsers();
    }

    public void loadUsers() throws IOException {
        File users = new File(USER_PATH);
        userList = objectMapper.readValue(users, new TypeReference<List<User>>() {
        });
    }

    public Boolean loginUser(String name, String pass) {
        Optional<User> foundUser = userList.stream().filter(user1 -> {
            return (user1.getName().equalsIgnoreCase(name)
                    && UserServiceUtil.checkPassword(pass, user1.getHashedPassword()));
        }).findFirst();
        return foundUser.isPresent();
    }

    public User getUser(String name, String pass) {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getName().equalsIgnoreCase(name)
                    && UserServiceUtil.checkPassword(pass, userList.get(i).getHashedPassword())) {
                return userList.get(i);
            }
        }
        return new User();
    }

    public Boolean signUp(User user1) {
        try {
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        } catch (IOException ex) {
            return Boolean.FALSE;
        }
    }

    public void saveUserListToFile() throws IOException {
        File userFile = new File(USER_PATH);
        objectMapper.writeValue(userFile, userList);
    }

    public void updateUserListToFile() throws IOException {
        File userListFile = new File(USER_PATH);
        List<User> newUserList = userList;
        newUserList.add(user);
        objectMapper.writeValue(userListFile, newUserList);
    }

    public void fetchBooking() {
        user.printTickets();
    }

    public Boolean cancelBooking(String ticketId) throws IOException {
        List<Ticket> updatedList = user.getTicketsBooked().stream().filter(e -> !e.getTicketId().equals(ticketId))
                .toList();
        user.setTicketsBooked(updatedList);
        saveUserListToFile();
        return Boolean.FALSE;
    }

    public List<Train> getTrains(String source, String dest) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, dest);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train) {
        return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat) {
        try {
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    trainService.addTrain(train);
                    return true; // Booking successful
                } else {
                    return false; // Seat is already booked
                }
            } else {
                return false; // Invalid row or seat index
            }
        } catch (IOException ex) {
            return Boolean.FALSE;
        }
    }
}
