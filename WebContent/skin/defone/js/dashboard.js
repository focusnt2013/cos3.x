$(function() {
	function showTooltip(x, y, contents) {
        $('<div id="tooltip" class="tooltipflot">' + contents + '</div>').css( {
          position: 'absolute',
          display: 'none',
          top: y + 5,
          left: x + 5
        }).appendTo("body").fadeIn(200);
     }
    
    var uploads = [[0, 9], [1, 8], [2, 5], [3, 8], [4, 5], [5, 14], [6, 10]];
    var downloads = [[0, 5], [1, 12], [2,4], [3, 3], [4, 12], [5, 11], [6, 15]];
    
    var plot = $.plot($("#website-stats"),
        [ { data: uploads,
          label: "Visits",
          color: successColor
        },
        { data: downloads,
          label: "Pages/Visit",
          color: infoColor
        }
      ],
      {
          series: {
             lines: {
            show: true,
            fill: true,
            lineWidth: 1,
            fillColor: {
              colors: [ { opacity: 0.5 },
                        { opacity: 0.5 }
                      ]
            }
          },
             points: {
            show: true
          },
          shadowSize: 0
          },
          legend: {
          position: 'nw'
        },
          grid: {
          hoverable: true,
          clickable: true,
          borderColor: '#efefef',
          borderWidth: 1,
          labelMargin: 10,
          backgroundColor: '#fff'
        },
        yaxis: {
          min: 0,
          max: 15,
          color: inverseColor
        },
        xaxis: {
          color: inverseColor
        }
    });
});
