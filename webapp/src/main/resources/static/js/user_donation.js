$(document).on("click", "ul.pagination li a", function() {
    let val = $(this).text().trim();

    let currPageNumber = $('li.page-item.active').find('a').text().trim();
    if (val === 'Next') {
        val = parseInt(currPageNumber) + 1;
    } else if (val === 'Previous') {
        val = parseInt(currPageNumber) - 1;
    }
    retrieveDonatedProjectForUser(event, val);
})

retrieveDonatedProjectForUser = (e, pageNumber) => {
    let url;
    if (pageNumber !== undefined) {
        url = `/user-detail/donation?page=${pageNumber}`;
    } else {
        url = '/user-detail/donation';
    }

    $("#donatedResult").load(url);
    e.preventDefault();
}