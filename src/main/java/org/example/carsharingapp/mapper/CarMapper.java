package org.example.carsharingapp.mapper;

import org.example.carsharingapp.config.MapperConfig;
import org.example.carsharingapp.dto.CarRequestDto;
import org.example.carsharingapp.dto.CarResponseDto;
import org.example.carsharingapp.dto.CarResponseLiteDto;
import org.example.carsharingapp.model.Car;
import org.example.carsharingapp.model.CarType;
import org.example.carsharingapp.model.TypeName;
import org.example.carsharingapp.repository.CarTypeRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = MapperConfig.class)
public abstract class CarMapper {

    @Autowired
    protected CarTypeRepository carTypeRepository;

    @Mapping(target = "type", expression = "java(mapType(requestDto.type()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    public abstract Car toModel(CarRequestDto requestDto);

    @Mapping(target = "type", expression = "java(car.getType() != null ? car.getType().getTypeName().toString() : null)")
    public abstract CarResponseDto toDto(Car car);

    public abstract CarResponseLiteDto toLiteDto(Car car);

    protected CarType mapType(String type) {
        TypeName typeName = TypeName.fromString(type)
                .orElseThrow(() -> new IllegalArgumentException("Invalid type name: " + type));

        return carTypeRepository.findByTypeName(typeName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid car type: " + type));
    }
}
