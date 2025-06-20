package ru.practicum.explore.user.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.explore.user.dto.NewUserDto;
import ru.practicum.explore.user.dto.RequestDto;
import ru.practicum.explore.user.dto.UserDto;
import ru.practicum.explore.user.model.Request;
import ru.practicum.explore.user.model.User;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapperNew {

    @SuppressWarnings("unused")          // может использоваться в тестах / будущем коде
    public static User mapToUser(UserDto dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return user;
    }

    public static User mapToUser(NewUserDto dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return user;
    }

    public static UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public static List<UserDto> mapToUserDto(Iterable<User> users) {
        List<UserDto> list = new ArrayList<>();
        users.forEach(u -> list.add(mapToUserDto(u)));
        return list;
    }

    public static List<RequestDto> mapToRequestDto(Iterable<Request> requests) {
        List<RequestDto> list = new ArrayList<>();
        requests.forEach(r -> list.add(mapToRequestDto(r)));
        return list;
    }

    public static RequestDto mapToRequestDto(Request r) {
        RequestDto dto = new RequestDto();
        dto.setId(r.getId());
        dto.setRequester(r.getRequesterId());
        dto.setEvent(r.getEventId());
        dto.setStatus(r.getStatus());
        dto.setCreated(
                r.getCreatedDate().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return dto;
    }
}
