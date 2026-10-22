package com.backend.form_to_sheets_api.component;

import org.springframework.stereotype.Component;

import com.backend.form_to_sheets_api.dto.FormRequestDto;
import com.backend.form_to_sheets_api.model.FormModel;

@Component
public class FormMapper {

    public FormModel toModel(FormRequestDto requestDto, String currentDateTime, String uniqueId) {
        FormModel formModel = new FormModel();

        // Metadatos generados automáticamente
        formModel.setId(uniqueId);


        // Datos principales del formulario
        formModel.setFullName(requestDto.getFullName());
        formModel.setNameOfOrganization(requestDto.getNameOfOrganization());
        formModel.setPosition(requestDto.getPosition());
        formModel.setAddress(requestDto.getAddress());
        formModel.setPhone(requestDto.getPhone());
        formModel.setEmail(requestDto.getEmail());
        formModel.setMembershipType(requestDto.getMembershipType());

        // Datos de contacto alternativo
        formModel.setFullAlternateName(requestDto.getFullAlternateName());
        formModel.setAlternatePosition(requestDto.getAlternatePosition());
        formModel.setAlternatePhoneNumber(requestDto.getAlternatePhoneNumber());

        // Pago y consentimientos
        formModel.setPaymentMethod(requestDto.getPaymentMethod());
        formModel.setContactSharingConsent(requestDto.getContactSharingConsent());
        formModel.setExternalOrgSharingConsent(requestDto.getExternalOrgSharingConsent());

        // Información detallada y uso del centro
        formModel.setOrganizationInfo(requestDto.getOrganizationInfo());
        formModel.setEntreUsageCapacity(requestDto.getCentreUsageCapacity()); // Mapeado desde el DTO corregido
        formModel.setOfficeWaitingListConsent(requestDto.getOfficeWaitingListConsent());
        formModel.setRequireBuildingKey(requestDto.getRequireBuildingKey());
        formModel.setDigitalSignature(requestDto.getDigitalSignature());

        // Fecha de registo
        formModel.setRegistrationDate(currentDateTime);

        return formModel;
    }
}