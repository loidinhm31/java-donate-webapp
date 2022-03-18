const drawBarChart = document.getElementById('barChart').getContext('2d');

loadDataBarChart = () => {
    let result = null;
    $.ajax({
        url: '/manage/dashboard/bar-chart',
        type: 'GET',
        async: false,
        success: function(data) {
            result = data;
        }
    });

    let labels = [];
    let statusOne = [];
    let statusTwo = [];
    let statusThree = [];
    let statusFour = [];

    $.each(result, function (index, value) {
        labels.push(index);

        $.each(value, function (innerKey, innerValue) {
            switch (innerKey) {
                case 0:
                    statusOne.push(innerValue);
                    break;
                case 1:
                    statusTwo.push(innerValue);
                    break;
                case 2:
                    statusThree.push(innerValue);
                    break;
                case 3:
                    statusFour.push(innerValue);
                    break;
            }
        });

    });

    let dataSet = [];
    for (let i = 0; i < 4; i++) {
        let label;
        let data;
        let background;
        let color;
        switch (i) {
            case 0:
                label = 'CREATED';
                data = statusOne;
                background = 'rgba(255, 99, 132, 0.5)';
                color = 'rgba(255, 99, 132, 1)';
                break;
            case 1:
                label = 'IN-PROGRESS';
                data = statusTwo;
                background = 'rgba(54, 162, 235, 0.5)';
                color = 'rgba(54, 162, 235, 1)';
                break
            case 2:
                label = 'ENDED';
                data = statusThree;
                background = 'rgba(255, 206, 86, 0.5)';
                color = 'rgba(255, 206, 86, 1)';
                break
            case 3:
                label = 'CLOSED';
                data = statusFour;
                background = 'rgba(153, 102, 255, 0.5)';
                color = 'rgba(153, 102, 255, 1)';
                break
        }

        const oneSet = {
            label: label,
            data: data,
            backgroundColor: background,
            borderColor: color,
            borderWidth: 1
        }
        dataSet.push(oneSet);
    }

    return {
        labels: labels,
        datasets: dataSet
    };
}


const barChart = new Chart(drawBarChart, {
    type: 'bar',
    data: loadDataBarChart(),
    options: {
        responsive: true,
        plugins: {
            legend: {
                position: 'right',
            },
            title: {
                display: true,
                text: '# of Projects By Month and Status'
            }
        }
    }
});

const drawLineChart = document.getElementById(('lineChart'));

const loadDataLineChart = () => {

    let projectId = $('#line-chart-select-project').val();

    let result = null;
    $.ajax({
        url: '/manage/dashboard/line-chart',
        type: 'GET',
        data: {
            projectId: projectId
        },
        async: false,
        success: function (data) {
            result = data;
        }
    });

    let labels = [];
    let totalInMonth = [];
    let accumulatedToMonth = [];

    $.each(result, function (index, value) {
        labels.push(index);

        $.each(value, function (innerKey, innerValue) {
            switch (innerKey) {
                case 0:
                    totalInMonth.push(innerValue);
                    break;
                case 1:
                    accumulatedToMonth.push(innerValue);
                    break;
            }
        });

    });

    let dataSet = [];
    for (let i = 0; i < 2; i++) {
        let label;
        let data;
        let background;
        let color;
        switch (i) {
            case 0:
                label = 'TOTAL IN MONTH';
                data = totalInMonth;
                background = 'rgba(255, 99, 132, 0.5)';
                color = 'rgba(255, 99, 132, 1)';
                break;
            case 1:
                label = 'ACCUMULATED TO MONTH';
                data = accumulatedToMonth;
                background = 'rgba(54, 162, 235, 0.5)';
                color = 'rgba(54, 162, 235, 1)';
                break
        }

        const oneSet = {
            label: label,
            data: data,
            backgroundColor: background,
            borderColor: color,
            borderWidth: 1
        }
        dataSet.push(oneSet);
    }

    return {
        labels: labels,
        datasets: dataSet
    };
}

var lineChart;
$('#line-chart-select-project').change(function () {
    console.log('complete')
    let data = loadDataLineChart();
    if (lineChart !== undefined) {
        lineChart.destroy();
    }

    lineChart = new Chart (drawLineChart, {
        type: 'line',
        data: data,
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'right',
                },
                title: {
                    display: true,
                    text: 'Amount For A Project By Month'
                }
            }
        },
    });

});