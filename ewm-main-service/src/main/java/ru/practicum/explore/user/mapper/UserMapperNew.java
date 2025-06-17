package ru.practicum.explore.user.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.explore.user.dto.RequestDto;
import ru.practicum.explore.user.dto.UserDto;
import ru.practicum.explore.user.model.Request;
import ru.practicum.explore.user.model.User;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapperNew {

    public static List<RequestDto> mapToRequestDto(Iterable<Request> requests) {
        List<RequestDto> result = new ArrayList<>();
        for (Request request : requests) {
            result.add(mapToRequestDto(request));
        }
        return result;
    }

    public static List<UserDto> mapToUserDto(Iterable<User> users) {
        List<UserDto> result = new ArrayList<>();
        for (User user : users) {
            result.add(mapToUserDto(user));
        }
        return result;
    }

    public static UserDto mapToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    public static User mapToUser(UserDto changeable) {
        User user = new User();
        user.setId(changeable.getId());
        user.setName(changeable.getName());
        user.setEmail(changeable.getEmail());
        return user;
    }

    public static RequestDto mapToRequestDto(Request changeable) {
        RequestDto requestDto = new RequestDto();
        requestDto.setId(changeable.getId());
        requestDto.setRequester(changeable.getRequesterId());
        requestDto.setEvent(changeable.getEventId());
        requestDto.setStatus(changeable.getStatus());
        requestDto.setCreated(changeable.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return requestDto;
    }
}