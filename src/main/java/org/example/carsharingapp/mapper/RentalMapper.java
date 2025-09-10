package org.example.carsharingapp.mapper;

import org.example.carsharingapp.config.MapperConfig;
import org.example.carsharingapp.dto.RentalResponseDto;
import org.example.carsharingapp.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {

    @Mapping(source = "car.brand", target = "brand")
    @Mapping(source = "car.model", target = "model")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(
            expression = "java(rental.getActualReturnDate() == null ? \"ACTIVE\" : \"FINISHED\")",
            target = "isActive"
    )
    RentalResponseDto toDto(Rental rental);
}
