$(document).on("click", "ul.pagination li a", function() {
    let val = $(this).text().trim();

    let currPageNumber = $('li.page-item.active').find('a').text().trim();
    if (val === 'Next') {
        val = parseInt(currPageNumber) + 1;
    } else if (val === 'Previous') {
        val = parseInt(currPageNumber) - 1;
    }
    retrieveProjects(event, val);
})

retrieveDonators = (e, projectId, pageNumber) => {

    let url;
    if (pageNumber !== undefined) {
        url = `/donation/donators/${projectId}?page=${pageNumber}`;
    } else {
        url = `/donation/donators/${projectId}`;
    }

    $("#donatorResult").load(url);
    e.preventDefault();
}

getValue = () => {
    // Get values for filter
    let filter = $('#project-status-select').val();

    return {
        projectStatuses: filter
    };
}

retrieveProjects = (e, pageNumber) => {

    let filters = getValue();

    let projectFilter = {
        startPage: pageNumber,
        filters: filters
    }

    let url = '/project';

    $.ajax({
        type: 'POST',
        url: url,
        data: JSON.stringify(projectFilter),
        contentType: 'application/json',
    }).done(function(data) {
        $('#projectContent').html(data);
    });
    e.preventDefault();
}

followProject = (e, projectId) => {

    let url = `/follower/${projectId}`;

    let btnFollow = $('button.follow');

    let val = (btnFollow.text().trim() === 'Follow');

    $.ajax({
        type: 'POST',
        url: url,
        data: {follow: val}
    }).done(function(data) {
        btnFollow.text((data ? 'Following' : 'Follow'));
    });
    e.preventDefault();
}