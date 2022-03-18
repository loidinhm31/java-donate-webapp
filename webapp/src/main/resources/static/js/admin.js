$(document).on("click", "ul.pagination li a", function() {
    let val = $(this).text().trim();

    let currPageNumber = $('li.page-item.active').find('a').text().trim();
    if (val === 'Next') {
        val = parseInt(currPageNumber) + 1;
    } else if (val === 'Previous') {
        val = parseInt(currPageNumber) - 1;
    }
    retrieveUsers(event, val);

})

$(document).on("click", "div#paginationRole ul.pagination li a", function() {
    let val = $(this).text().trim();

    let currPageNumber = $('div#paginationRole li.page-item.active').find('a').text().trim();
    if (val === 'Next') {
        val = parseInt(currPageNumber) + 1;
    } else if (val === 'Previous') {
        val = parseInt(currPageNumber) - 1;
    }
    retrieveUsersInRole(event, val);

})

clearFilterByLogin = (e) => {
    $('input[name="option"]').prop('checked', false);
    e.preventDefault();
}

retrieveUsers = (e, pageNumber) => {
    let searchValue = $('input#searchValue').val();

    let filterValue = $('input[name="option"]:checked').val();

    if (filterValue === undefined) {
        filterValue = '';
    }

    let url;
    if (pageNumber !== undefined) {
        url = `/admin/user/search?page=${pageNumber}&searchKey=${searchValue}&option=${filterValue}`;
    } else {
        url = `/admin/user/search?&searchKey=${searchValue}&option=${filterValue}`;
    }

    $("#searchResult").load(url);
    e.preventDefault();
}


retrieveUsersInRole = (e, pageNumber) => {
    let searchValue = $('input#searchValueForRole').val();

    let url;
    if (pageNumber !== undefined) {
        url = `/admin/user/role/search?page=${pageNumber}&searchKey=${searchValue}`;
    } else {
        url = `/admin/user/role/search?&searchKey=${searchValue}`;
    }

    $("#roleResult").load(url);
    e.preventDefault();
}

retrieveRoles = (e, userId) => {
    let url = `/admin/user/roles/select?userId=${userId}`;

    $("#admin-roles-select").load(url);
    e.preventDefault();
}

updateRole = (user, e) => {
    let url = '/manage/users/role/save';

    let currentRoles = $('form#role-form input[type=checkbox]');

    let roles = [];
    currentRoles.each((i, el) => {
        if (el.checked) {
            let userRole = {
                role: {
                    roleId: el.id,
                    check: true
                }
            }
            roles.push(userRole);
        } else {
            let userRole = {
                role: {
                    roleId: el.id,
                    check: false
                }
            }
            roles.push(userRole);
        }
    });
    // user.userRoles = [];
    user.userRoles = roles;

    $.ajax({
        type: 'POST',
        url: url,
        data: JSON.stringify(user),
        contentType: 'application/json'
    }).done(() => {
        const roleModal = $('#modalPermissionBody');
        roleModal.hide();

        $('.modal-backdrop').remove();
        roleModal.modal('dispose');
        retrieveUsersInRole(e, undefined);
    }).fail(() => {
        console.log('fail');
    });
}

updatePermissionForManyUsers = (e) => {

    // Get selected user(s)
    const checkList =  $('div.active-role input[type=checkbox]:checked');
    let users = [];
    checkList.each((i, el) => {
        if (el.checked) {
            users.push(el.id);
        }
    })

    // Get permission
    let permission = $('form#role-form-multiple input[type=radio]:checked');

    let active;

    if (permission.val() === 'enable') {
        active = true;
    } else if (permission.val() === 'disable') {
        active = false;
    }

    const roleRequest = {
        users: users,
        active: active
    }

    let url = '/manage/users/role';
    $.ajax({
        type: 'POST',
        url: url,
        data: JSON.stringify(roleRequest),
        contentType: "application/json"
    }).done(() => {
        const roleMultipleModal = $('#modalPermissionMultipleBody');
        roleMultipleModal.hide();

        $('.modal-backdrop').remove();
        roleMultipleModal.modal('dispose');
        retrieveUsersInRole(e, undefined);
    }).fail(() => {
        console.log('fail');
    });
    e.preventDefault();
}

exportExcelUsers = () => {

    let searchValue = $('input#searchValue').val();

    let filterValue = $('input[name="option"]:checked').val();

    if (filterValue === undefined) {
        filterValue = '';
    }

    let api = `/manage/users/export-excel?&searchKey=${searchValue}&option=${filterValue}`;

    window.open(api);
}

changeActive = (e, userId) => {
    let isEnable = $('input#' + userId).is(':checked');

    let url = `/manage/users/active/${userId}/${isEnable}`;
    $.ajax({
        type: 'POST',
        url: url,
        contentType: "application/json"
    }).done(() => {
        retrieveUsersInRole(e, undefined);
    }).fail(() => {
        console.log('fail');
    });
    e.preventDefault();
}