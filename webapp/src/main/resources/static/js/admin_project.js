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

$('.selectpicker').selectpicker();

addValue = (selectorId) => {
    let valueSelect = $(`#value${selectorId}`);
    let value = valueSelect.val().trim();

    // Get the last el
    let lastEl = $('table tbody tr td ul li.list-group-item').last();

    let inputId;
    if (lastEl.length > 0) {
        let lastElVal = lastEl.attr('id')
        inputId = lastElVal.substring(lastElVal.indexOf("-") + 1);
    }

    if (value !== '') {
        $(`#select${selectorId}`).append(`<li class="list-group-item rounded-pill mx-1 my-1" id="input-${inputId !== undefined ? (parseInt(inputId) + 1) : 1}">
                                  ${value} 
                                  <span class="btn  mx-1 p-0 border-1"
                                    onclick="deleteValue(this)">
                                    <i class="fas fa-window-close"></i>
                                  </span>
                          </li>`);
    }

    valueSelect.val('');
}

deleteValue = (el) => {
    const deleteEl = $(el).parent();
    deleteEl.remove();
}

getValue = () => {
    // Get values for criteria 1
    let valuesOfOne = [];
    $('table tbody tr td.value-1 ul li').each((i, el) => {
        valuesOfOne.push($(el).text().trim());
    })

    // Get values for criteria 2
    let valuesOfTwo = $('#project-select').val();

    // Get values for criteria 3
    let valuesOfThree = $('#status-select').val();

    return {
        phoneNumbers: valuesOfOne,
        projectCodes: valuesOfTwo,
        projectStatuses: valuesOfThree
    };
}

retrieveProjects = (e, pageNumber) => {

    let filters = getValue();

    let projectFilter = {
        startPage: pageNumber,
        filters: filters
    }

    let url = '/admin/project/search';

    $.ajax({
        type: 'POST',
        url: url,
        data: JSON.stringify(projectFilter),
        contentType: 'application/json',
    }).done(function(data) {
        $('#searchProjectResult').html(data);
    });
    e.preventDefault();
}

retrieveProjectTime = (e, projectId) => {
    let url = `/admin/project/time-extend?projectId=${projectId}`;

    $("#timeDetail").load(url);
    e.preventDefault();
}

updateProjectTime = (e, projectId) => {
    let url = '/manage/projects/time-extend';

    let timeVal = $('#targetDate').val();

    const timeModal = $('#timeModal');

    $.ajax({
        type: 'POST',
        url: url,
        data: JSON.stringify({
            projectId: projectId,
            targetTime: timeVal
        }),
        contentType: 'application/json'
    }).done(() => {
        timeModal.hide();
        $('.modal-backdrop').remove();
        timeModal.modal('dispose');
        retrieveProjects(e, undefined);
    }).fail((response) => {
        timeModal.hide();
        $('.modal-backdrop').remove();
        timeModal.modal('dispose');

        alert(response.responseJSON.message);
    });
}

retrieveProjectDelete = (e, projectId) => {
    let url = `/admin/project/delete?projectId=${projectId}`;

    $("#projectDelete").load(url);
    e.preventDefault();
}

deleteInitProject = (e, projectId) => {
    let url = '/manage/projects';
    $.ajax({
        type: "DELETE",
        url: url,
        data: {projectId: projectId}
    }).done(() => {
        const deleteModal = $('#deleteInitProjectModal');
        deleteModal.hide();

        $('.modal-backdrop').remove();
        deleteModal.modal('dispose');
        retrieveProjects(e, undefined);
    });
}

editProject = (projectId) => {
    $("<form/>",
        {
            action:`/admin/project/edit/${projectId}` ,
            method: 'GET'
        }
    ).appendTo('body').submit();
}