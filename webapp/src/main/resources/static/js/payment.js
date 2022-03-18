var amountField = $('#donateAmountWithCurrency');
var rootAmount;
$('#paymentForm').change(() => {
    let radioVal = $("input[name='paymentRadio']:checked").val();

    if (radioVal === 'PAYPAL') {
        $('#confirmDonatePaypal').show();
        $('#paymentButton').hide();
        $('#paymentInformationForm p').text('USD');

        // Convert currency
        let amount = amountField.val();
        // Save amount
        rootAmount = amount;

        let exchangeRate = getExchangeRate('USD');


        let convertedAmount = amount / exchangeRate;

        amountField.val(convertedAmount);
    } else {
        $('#confirmDonatePaypal').hide();
        $('#paymentButton').show();
        $('#paymentInformationForm p').text('VND');

        // Keep currency
        amountField.val(rootAmount);
    }
});

$('#confirmDonatePaypal').on('click', () => {
    // Hide confirm button
    $('#confirmDonatePaypal').parent().hide();

    // Disable all others options after button was confirmed
    $('input#manualRadio').prop('disabled', true);

    let currAmount = amountField.val();

    PayPal.Donation.Button({
        env: 'sandbox',
        business: 'JKYT2UD2DX4D8',
        image: {
            src: 'https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif',
            title: 'PayPal - The safer, easier way to pay online!',
            alt: 'Donate with PayPal button'
        },
        amount: currAmount,
        onComplete: function (params) {
            validatePaypalTransaction(params.tx);
        },
    }).render('#paypal-donate-button-container');
})

sendPaymentManualRequest = () => {
    $('input#projectId').prop('disabled', false);
    $('#paymentInformationForm').submit();
}

validatePaypalTransaction = (transactionId) => {
    $('#transactionId').val(transactionId);
    $('#methodType').val('PAYPAL')
    $('#transactionForm').submit();
}

getExchangeRate = (currency) => {

    let result = null;
    $.ajax({
        url: '/payment/exchange-rate',
        type: 'GET',
        async: false,
        data: {currency: currency},
        success: function(data) {
            result = data;
        }
    });
    return result;
}