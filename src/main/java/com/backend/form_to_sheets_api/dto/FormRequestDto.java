package com.backend.form_to_sheets_api.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class FormRequestDto {

    @NotBlank(message = "Full name is mandatory")
    @Size(min = 3, max = 100, message = "The name must be between 3 and 100 characters.")
    private String fullName;

    @Size(min = 3, max = 100, message = "The name of organization must be between 3 and 100 characters.")
    private String nameOfOrganization;
    @Size(min = 3, max = 100, message = "The position must be between 3 and 100 characters.")
    private String position;

    @NotBlank(message = "Address is mandatory")
    @Size(min = 3, max = 100, message = "The address must be between 3 and 100 characters.")
    private String address;

    @NotBlank(message = "Phone Number is mandatory")
    @Size(min = 5, max = 15, message = "The phone number must be between 5 and 15 characters.")
    private String phone;

    @NotBlank(message = "Email is mandatory")
    @Email( message = "You must provide a valid email format.")
    private String email;

    @NotBlank(message = "Membership Type is mandatory")
    @Pattern(regexp = "^(Organization|Individual)$", message = "The value must be strictly: 'Organization' or 'Individual'")
    private String membershipType;

    @Size(min = 3, max = 100, message = "The name must be between 3 and 100 characters.")
    private String fullAlternateName;
    @Size(min = 3, max = 100, message = "The position must be between 3 and 100 characters.")
    private String alternatePosition;


    @Size(min = 5, max = 15, message = "The phone number must be between 5 and 15 characters.")
    private String alternatePhoneNumber;

    @NotBlank(message = "Payment Method is mandatory")
    @Pattern(regexp = "^(e-transfer|receive an invoice|drop off a cheque)$", message = "The value must be strictly: 'e-transfer', 'receive an invoice' or 'drop off a cheque'")
    private String paymentMethod;

    @NotBlank(message = "Contact Sharing Consent is mandatory")
    @Pattern(regexp = "^(Yes)$", message = "The value must be strictly: 'Yes'")
    private String contactSharingConsent;

    @NotBlank(message = "External Organization Sharing Consent is mandatory")
    @Pattern(regexp = "^(Yes)$", message = "The value must be strictly: 'Yes'")
    private String externalOrgSharingConsent;

    @Size(min = 20, max = 500, message = "The organization info must be between 20 and 500 characters.")
    private String organizationInfo;

    @Size(min = 20, max = 500, message = "The centre usage capacity must be between 20 and 500 characters.")
    private String centreUsageCapacity;

    @Pattern(regexp = "^(Yes|No)$", message = "The value must be strictly: 'Yes' or 'No'")
    private String officeWaitingListConsent;

    @NotBlank(message = "Require building key is mandatory")
    @Pattern(regexp = "^(Yes|No)$", message = "The value must be strictly: 'Yes' or 'No'")
    private String requireBuildingKey;

    @NotBlank(message = "Digital Signature key Method is mandatory")
    private String digitalSignature;

    //  No olvidar activarlo para producción
//    @NotBlank(message = "Turnstile token is mandatory")
    private String turnstileToken;
}
