package com.backend.form_to_sheets_api.model;

import lombok.Getter;
import lombok.Setter;




@Getter @Setter
public class FormModel {
    private String id; //
    private String fullName;//
    private String nameOfOrganization;//
    private String position;//
    private String address;
    private String phone;
    private String email;
    private String membershipType;
    private String fullAlternateName;
    private String alternatePosition;
    private String alternatePhoneNumber;
    private String paymentMethod;
    private String contactSharingConsent;
    private String externalOrgSharingConsent;
    private String organizationInfo;
    private String entreUsageCapacity;
    private String officeWaitingListConsent;
    private String requireBuildingKey;
    private String digitalSignature;
    private String registrationDate;



}
