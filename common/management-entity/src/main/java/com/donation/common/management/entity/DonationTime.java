package com.donation.common.management.entity;

import com.donation.common.core.enums.PaymentMethodType;
import com.donation.common.core.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

import static com.donation.common.management.SchemaConstant.DONATION_SCHEMA;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "donation_time", schema = DONATION_SCHEMA)
public class DonationTime extends BaseEntity<String> {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "supporter_name")
    private String supporterName;

    @Column(name = "supporter_email")
    private String supporterEmail;

    @Column(name = "is_display_name")
    private Boolean isDisplayName = true;

    @Column(name = "transaction_code", unique = true)
    private String transactionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethodType paymentMethod;

    @Column(name = "message")
    private String message;

    @Column(name = "amount")
    private Float amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserAccountDetail userAccountDetail;

}
