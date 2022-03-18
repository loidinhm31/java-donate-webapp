package com.donation.data.handler.service;

public interface GenerateIdService {
    String generateProjectId(String projectName) throws Exception;

    String generateBeneficiaryId(String beneficiaryName) throws Exception;

    String generateTransferId(String projectId);
}
