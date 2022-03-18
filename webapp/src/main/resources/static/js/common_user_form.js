
function checkPasswordMatch(confirmPassword) {
	if (confirmPassword.value !== $("#password").val()) {
		confirmPassword.setCustomValidity("Password do not match!");
	} else {
		confirmPassword.setCustomValidity("");
	}
}

function checkUsernameUnique(event) {
	let username = $("input#input-username").val();
	let infoUsername = $("p#info-username");
	if (username.length > 2) {
		infoUsername.removeAttr('hidden');

		let url = "/users/username/isUnique";

		let csrfValue = $("input[name='_csrf']").val();
		console.log(csrfValue);

		let params = {username: username, _csrf: csrfValue};
		$.post(url, params, function(response) {
			if (response === 'OK') {
				infoUsername.text('You can use this username');
			} else {
				infoUsername.text('This username has been used');
			}
		}).fail(function() {
			infoUsername.text('This username has been used');
		});
	} else if (username.length === 0) {
		infoUsername.attr('hidden', true);
	}
	event.preventDefault();
}

function checkEmailUnique(event) {
	let userEmail = $("input#input-email").val();
	let infoEmail = $("p#info-email");
	if (userEmail.length > 2
		&& userEmail.indexOf('@') !== -1
		&& userEmail.indexOf(".") !== -1) {
		infoEmail.removeAttr('hidden');

		let url = "/users/email/isUnique";
		let csrfValue = $("input[name='_csrf']").val();

		let params = {email: userEmail, _csrf: csrfValue};
		$.post(url, params, function(response) {
			if (response === 'OK') {
				infoEmail.text('You can use this email')
			} else {
				infoEmail.text('This email has been used');
			}
		}).fail(function() {
			infoEmail.text('This email has been used');
		});
	} else if (userEmail.length === 0) {
		infoEmail.attr('hidden', true);
	}
	event.preventDefault();
}