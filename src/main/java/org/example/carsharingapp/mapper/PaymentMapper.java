package org.example.carsharingapp.mapper;

import org.example.carsharingapp.config.MapperConfig;
import org.example.carsharingapp.dto.PaymentResponseDto;
import org.example.carsharingapp.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {

    @Mapping(target = "status", expression = "java(payment.getStatus().getPaymentStatusName().name())")
    @Mapping(target = "type", expression = "java(payment.getType().getPaymentTypeName().name())")
    @Mapping(target = "brand", expression = "java(payment.getRental().getCar().getBrand())")
    @Mapping(target = "model", expression = "java(payment.getRental().getCar().getModel())")
    PaymentResponseDto toDto(Payment payment);
}
