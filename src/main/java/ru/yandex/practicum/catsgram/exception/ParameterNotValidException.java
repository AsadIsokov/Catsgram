package ru.yandex.practicum.catsgram.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class ParameterNotValidException extends IllegalArgumentException{
    private final String parameter;
    private final String reason;
}
