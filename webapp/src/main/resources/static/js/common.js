sendDonateForm = () => {
    // Check email
    const inputEmail = $('input#email');
    if (inputEmail.length > 0 && inputEmail.val().trim() === '') {
        alert('Email cannot be empty');
    } else {
        let hideInformationField = $('#isDisplayName');
        let isHideInformation = hideInformationField.val();
        hideInformationField.val(!isHideInformation)
        $('#donateInformationForm').submit();
    }


}

sendDonateFormFree = () => {
    // Check email
    const inputEmail = $('input#emailFree');
    if (inputEmail.length > 0 && inputEmail.val().trim() === '') {
        alert('Email cannot be empty');
    } else {
        let hideInformationField = $('#isDisplayNameFree');
        let isHideInformation = hideInformationField.val();
        hideInformationField.val(!isHideInformation)
        $('#donateInformationFormFree').submit();
    }
}

retrieveProjectList = (e) => {
    let url = '/project/list';
    $("#donateContentFree").load(url);
    e.preventDefault();
}