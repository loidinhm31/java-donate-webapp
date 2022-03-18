$(document).on("click", "ul.pagination li a", function() {
    let val = $(this).text().trim();

    let currPageNumber = $('li.page-item.active').find('a').text().trim();
    if (val === 'Next') {
        val = parseInt(currPageNumber) + 1;
    } else if (val === 'Previous') {
        val = parseInt(currPageNumber) - 1;
    }
    retrieveProjectsByFollower(event, val);
})

retrieveProjectsByFollower = (e, pageNumber) => {
    let url;
    if (pageNumber !== undefined) {
        url = `/user-detail/follower?page=${pageNumber}`;
    } else {
        url = '/user-detail/follower';
    }

    $("#followResult").load(url);
    e.preventDefault();
}