(function($, window) {
    // generate a random number
    window.randNum = function(){
        return (Math.floor( Math.random()* (1+40-20) ) ) + 20;
    };

    var slimScrollSettings = {
		alwaysVisible: true,
        railVisible: true,
        railColor: '#222',
        railOpacity: 0.08,
        size: '4px'
    };
    
    // Function to generate sparklines
    function generateSparklines()
    {
        if ($('.sparkline').length)
        {
            $.each($('.sparkline'), function(k,v)
            {
                var size = { w: 150, h: 28 };
                var color = primaryColor;
                
                if ($(this).is('.danger')) color = dangerColor;
                if ($(this).is('.success')) color = successColor;
                if ($(this).is('.warning')) color = warningColor;
                if ($(this).is('.inverse')) color = inverseColor;
                if ($(this).is('.info')) color = infoColor;
                
                var data = [[1, 3+randNum()], [2, 5+randNum()], [3, 8+randNum()], [4, 11+randNum()],[5, 14+randNum()],[6, 17+randNum()],[7, 20+randNum()], [8, 15+randNum()], [9, 18+randNum()], [10, 22+randNum()]];
                $(v).sparkline(data, 
                { 
                    type: 'bar',
                    width: size.w,
                    height: size.h,
                    stackedBarColor: [color, color],
                    lineWidth: 2
                });
            });
        }

        if ($('.sparkline-line').length)
        {
            $.each($('.sparkline-line'), function(k,v)
            {
                var size = { w: 28, h: 28 };
                
                var data = [3+randNum(), 5+randNum(), 8+randNum(), 11+randNum()];
                $(v).sparkline(data, 
                { 
                    type: 'pie',
                    width: size.w,
                    height: size.h,
                    sliceColor: [dangerColor, warningColor, infoColor, successColor],
                });
            });
        }
    }

    // Initialize DateRangePicker where it is needed
    if ( $('#reportrange').length > 0 )
    {
        $('#reportrange').daterangepicker(
            {
                ranges: {
                    'Today': [moment(), moment()],
                    'Yesterday': [moment().subtract('days', 1), moment().subtract('days', 1)],
                    'Last 7 Days': [moment().subtract('days', 6), moment()],
                    'Last 30 Days': [moment().subtract('days', 29), moment()],
                    'This Month': [moment().startOf('month'), moment().endOf('month')],
                    'Last Month': [moment().subtract('month', 1).startOf('month'), moment().subtract('month', 1).endOf('month')]
                },
                startDate: moment().subtract('days', 29),
                endDate: moment()
            },
            function(start, end) {
                $('#reportrange span').html(start.format('MMMM D, YYYY') + ' - ' + end.format('MMMM D, YYYY'));
            }
        );
    }
    /* Easy pie charts
    if ($('.easy-pie').length)
    {
        $.each($('.easy-pie'), function(k,v)
        {
            if ($(this).is('.danger')) color = (dangerColor) ? dangerColor : '#F22613';
            if ($(this).is('.success')) color = (successColor) ? successColor : '#2ecc71';
            if ($(this).is('.warning')) color = (warningColor) ? warningColor : '#F5AB35';
            if ($(this).is('.inverse')) color = (inverseColor) ? inverseColor : '#111';
            if ($(this).is('.info')) color = (infoColor) ? infoColor : '#3498db';

            $(this).easyPieChart({
                barColor: color,
                lineWidth: 5,
                trackColor: '#efefef',
                scaleColor: '#000',
                easing: 'easeOutBounce',
                onStep: function(from, to, percent) {
                    $(this.el).find('.percent').text(Math.round(percent));
                }
            });
            var chart = window.chart = $('.easy-pie').data('easyPieChart');
        });
    }*/


    // Initialize sidebar menu
    if ( $("#side-menu").length )
        $('#side-menu').metisMenu();

    // add functionality to .action-link anchors
    $('.action-link').on('click', function (e) {
        // console.log($(this).closest('.dropdown'));
        var link = $(this);
        link.closest('.dropdown').addClass('open');
        // e.preventDefault();
        $(this).popover({
            placement: 'bottom',
            container: 'body',
            title: $(this).data('title') || 'Element Title',
            html: true,
            content: '<div class="input-group"><input type="text" class="form-control" placeholder="Write your message here..."><span class="input-group-btn"><button class="btn btn-success" type="button">Go!</button></span></div>'
        });
        var pop = $(this).popover('show');
        $(pop).on('shown.bs.popover', function (e) {
            console.log($('textarea'));
            $('textarea').on('click', function (e) {
                console.log(link);
                link.closest('.dropdown').addClass('open');
                e.stopPropagation();
            });
        });
        e.stopPropagation();
    });

    // Initializing tooltips
    if ( $('[data-toggle="tooltip"]').length )
    {
        $('[data-toggle="tooltip"]').tooltip();
    }

    // Initializing popovers
    if ( $('[data-toggle="popover"]').length )
    {
        $('[data-toggle="popover"]').popover();
    }

    // Initializing select2 plugin
    if ( $('[data-toggle="select2"]').length )
        $('[data-toggle="select2"]').select2();

    $('.trigger-sidebar').on( 'click', function () {
        if ($('.sidebar-right').length) {
            if ($('.sidebar-right').hasClass('open')) {
                $('.sidebar-right').removeClass('open');
                $('.navbar').removeClass('sidebar-open');
                $('body').removeClass('menu-hidden');
                $('footer').removeClass('sidebar-open');
            } else {
                $('body').addClass('menu-hidden');
                $('.sidebar-right').addClass('open');
                $('.navbar').addClass('sidebar-open');
                $('footer').addClass('sidebar-open');
            }
        }
    });

    $('.navbar-toggle').on('click', function () {
        if ($('#menu.hidden-xs').length)
            $('#menu').removeClass('hidden-xs');
        else
            $('#menu').addClass('hidden-xs');
    });
    
    //Loads the correct sidebar on window load
    $(window).bind("load", function() {
        if ($(this).width() < 768) {
            $('div.sidebar-collapse').addClass('collapse');
        } else {
            $('div.sidebar-collapse').removeClass('collapse');
        }
    });

    // Switch theme color
    var $styleSwitcher = $('#style_switcher');
    $('.switcher_toggle').on('click', function(e) {
        if(!$styleSwitcher.hasClass('switcher_open')) {
            $styleSwitcher.addClass('switcher_open');
        } else {
            $styleSwitcher.removeClass('switcher_open');
        }
        e.preventDefault();
    });
    
    $('.switch-style').on('click', function (event) {
        $('#style_switcher li').removeClass('style_active');
        $(this).addClass('style_active');
        var themeStyle = $( this ).attr( 'data-style' );
        $.cookie('themeStyle', themeStyle);
        var rsp = $.cookie('dev');
        if ( rsp == 'false' )
        {
            $("#style_color").attr('href', 'skin/defone/css/'+themeStyle+'.css');
        }
        window.location.reload();
        return false;
    });


    //  Fixing the navbar to pe fixed or static
    $('#fixed_navbar').on('click', function () {
        if ( $(this).is(':checked') )
        {
            $('.navbar.navbar-default').removeClass('navbar-static-top').addClass('navbar-fixed-top');
            $('#page-wrapper').addClass('fixed-navbar');
        }
        else
        {
            $('.navbar.navbar-default').removeClass('navbar-fixed-top').addClass('navbar-static-top');
            $('#page-wrapper').removeClass('fixed-navbar');
            $('#menu').removeClass('navbar-fixed-side').addClass('navbar-static-side');
            $( '#menu' ).getNiceScroll().remove();
        }
    });

    // As by default we have the fixed sidebar
    // So we have to add niceScroll to it
    if ( $('#menu').length )
        $( '#menu' ).niceScroll({cursorcolor: '#fff',railalign: 'right', cursorborder: "none", horizrailenabled: false, zindex: 2001, left: '245px', cursoropacitymax: 0.6, cursorborderradius: "0px", spacebarenabled: false });
    if ( $('#dropdown-menu-notifies').length )
        $( '#dropdown-menu-notifies' ).niceScroll({cursorcolor: '#fff',railalign: 'right', cursorborder: "none", horizrailenabled: false, zindex: 2001, left: '245px', cursoropacitymax: 0.6, cursorborderradius: "0px", spacebarenabled: false });

    
    //  Fixing the sidebar to pe fixed or static
    $('#fixed_sidebar').on('click', function () {
        if ( $(this).is(':checked') )
        {
            $('#menu').removeClass('navbar-static-side').addClass('navbar-fixed-side');
            $( '#menu' ).niceScroll({cursorcolor: '#fff', cursorborder: "none", horizrailenabled: false, zindex: 2000, cursoropacitymax: 0.3, cursorborderradius: "0px", spacebarenabled: false });
        }
        else
        {
            $('#menu').removeClass('navbar-fixed-side').addClass('navbar-static-side');
            $( '#menu' ).getNiceScroll().remove();
            $('.navbar.navbar-default').removeClass('navbar-fixed-top').addClass('navbar-static-top');
            $('#page-wrapper').removeClass('fixed-navbar');
        }
    });


    //  Fixing the sidebar to pe fixed or static
    $('#fixed_footer').on('click', function () {
        if ( $(this).is(':checked') )
        {
            $('footer').addClass('fixed-footer');
            $('#page-wrapper').addClass('m-b-60');
        }
        else
        {
            $('footer').removeClass('fixed-footer');
            $('#page-wrapper').removeClass('m-b-60');
        }
    });



    // Calculating the height of the window/client
    var windowHeight =  Math.max(document.documentElement.clientHeight, window.innerHeight || 0);

    $('.sidebar-right .tab-pane').each(function () {
        $(this).css('height', windowHeight-66);
        $(this).niceScroll({cursorcolor: "#fff", cursorborder: "none", horizrailenabled: false, zindex: 9999, cursorborderradius: "0px", spacebarenabled: false });
    });

    $('.col-sm-4').each(function () {
        $(this).css('height', 640);
        $(this).niceScroll({cursorcolor: "#fff", cursorborder: "none", horizrailenabled: false, zindex: 9999, cursorborderradius: "0px", spacebarenabled: false });
    });
    
    $(window).resize(function () {
        var windowHeight =  Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
        $('.sidebar-right .tab-pane').each(function () {
            $(this).css('height', windowHeight-66);
        });
    });

    //Collapses the sidebar on window resize
    $(window).bind("resize", function() {
        if ($(this).width() < 768) {
            $('div.sidebar-collapse').addClass('collapse');
        } else {
            $('div.sidebar-collapse').removeClass('collapse');
        }
    });



    // Adding niceScroll to HTML tag
    //if ( typeof cursorColor != "undefined" )
    //    $( "html" ).niceScroll({cursorcolor: cursorColor, cursorborder: "none", horizrailenabled: false, zindex: 2000, cursorborderradius: "0px", spacebarenabled: false });

    $('.has-nice-scroll').each(function () {
        $(this).niceScroll({
            horizrailenabled: false, 
            zindex: 2000,
            cursorborder: "none",
        });
    });

    // Stopping Dropdown menu from closing on click event
    $('.mega-menu .dropdown-menu').on('click', function (event) {
        event.stopPropagation();
    });

    generateSparklines();


    // removing panels
    $('[data-action^="close"]').on('click', function () {
      $(this).closest('.panel').hide();
    });

    // realoding panels
    $('[data-action^="reload"]').on('click', function () {
      $(this).closest(".panel").children('.panel-body').block({ 
        message: '<h2><i class="fa fa-spinner fa-spin"></i></h2>',
        css: { 
          border: 'none', 
          padding: '15px', 
          background: 'none',
        },
        overlayCSS: { backgroundColor: '#FFF' },
        timeout: 2000 
      });
    });
    // panle settings
    $('[data-action^="settings"]').on('click', function () {

    });
    // panle minimize
    $('[data-action^="minimize"]').on('click', function () {
      if ($(this).hasClass('active')){
        $(this).removeClass('active');
        $(this).closest(".panel").children('.panel-body').slideDown('fast'); 
      } else{
        $(this).addClass('active');
        $(this).closest(".panel").children('.panel-body').slideUp('fast'); 
      }
    });


    //Set the carousel options
    $('#quote-carousel').carousel({
        pause: true,
        interval: 4000,
    });

    // Page Heading background attribute setter
    if ( $('.page-header').length > 0 )
    {
        $this = $('.page-header');
        var background = $this.data('background');
        var text_color = $this.data('text-color');
        var text_shadow = $this.data('text-shadow');

        // Setting the background image for the page header
        if ( typeof background !== typeof undefined && background !== false )
        {
            $this.css('background', 'url('+background+') center center no-repeat');
        }
        // Setting text color if backgtround image is to dark for the default color of heading text
        if ( typeof text_color !== typeof undefined && text_color !== false )
        {
            $this.children('.heading').css('color', text_color).children('.sub-heading').css('color', text_color);
        }
        if ( typeof text_shadow !== typeof undefined && text_shadow !== false )
        {
            $this.children('.heading').css('text-shadow', '0 2px 0 rgba(0, 0, 0, 0.5)');
        }
    }


    // If page Login or Register is viewed set the height of the content
    if ( $('.full-height').length > 0 )
    {
        var windowHeight =  Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
        $('.full-height').css('min-height', windowHeight);
    }

    // If page 404 or 500 is viewed set the height of the content
    if ( $('#error').length > 0 )
    {
        var windowHeight =  Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
        $('#page-wrapper').css('min-height', windowHeight);
    }

    // Demonstration purpose ONLY
    if ( $( '#ionicons' ).length > 0 )
    {
        $('#ionicons .icon-section .icon-section .icon-container').each(function () {
            var span = $(this).children('span:first-child');
            var iconname = span.attr('class');
            $(this).append('<span class="icon-name">'+iconname+'</span>');
            
            // console.log(namecontainer);
        });
    }

    // Initializing SWITCHERY plugin
    var elems = Array.prototype.slice.call(document.querySelectorAll('.js-switch'));

    elems.forEach(function(html) {
        var switchery = new Switchery(html);
    });



    // Adding slimScroll to chat lists
    slimScrollSettings.height =  windowHeight-440;
    $('#chat #divViewPanel').slimScroll(slimScrollSettings);
    $('#chat #divDeveloperPanel').slimScroll(slimScrollSettings)
    $('#chat a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
    });

    // Setting chat-windows max-height to fit perfectly in the window
    slimScrollSettings.height =  windowHeight-50;
    $('.chat-window .chat-messages').slimScroll(slimScrollSettings);  
    // Separate window resize listener for everything else then the sidebar
    $(window).resize(function () {
        // Adding slimScroll to chat lists
        slimScrollSettings.height =  windowHeight-440;
        $('#chat #divViewPanel').slimScroll(slimScrollSettings);
        $('#chat #divDeveloperPanel').slimScroll(slimScrollSettings)
        slimScrollSettings.height =  windowHeight-108;
        $('#chat a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
            if ( e.target.hash == '#cfg_developers' )
            {
            }
        });
    });


    var body = document.body,
        mask = document.createElement("div"),
        navigationMask = document.createElement("div"),
        toggleSlideRight = document.querySelector( ".chat" ),
        // toggleSlideRightMobile = document.querySelector( ".chat-mobile" ),
        // toggleNavigation = document.querySelector( ".toggle-navigation" ),
        // toggleNavigationMobile = document.querySelector( ".toggle-nav" ),
        activeNav
    ;

    mask.className = "mask";
    navigationMask.className = "navigation-mask";
    toggleSlideRight.addEventListener( "click", function(){
        classie.add( body, "smr-open" );
        document.body.appendChild(mask);
        activeNav = "smr-open";
    });

    mask.addEventListener( "click", function(){
        classie.remove( body, activeNav );
        classie.remove( body, 'chat-window-open');
        activeNav = "";
        document.body.removeChild(mask);
    } );

    navigationMask.addEventListener( "click", function(){
        if (activeNav == "sb-open")
        {
            classie.remove( body, activeNav );
            classie.add( body, "sb-hidden" );
        } else {
            classie.remove( body, activeNav );
            classie.add( body, "sb-open" );
        }
        document.body.removeChild(navigationMask);
    } );



    [].slice.call(document.querySelectorAll(".close-chat")).forEach(function(el,i){
        el.addEventListener( "click", function(){
            classie.remove( body, activeNav );
            activeNav = "";
            document.body.removeChild(mask);
        } );
    });


    // Opening up chat window
    $('.user-chat, .close-chat-window').on('click', function () {
        var body = document.body;
        if ( classie.has( body, 'chat-window-open') )
            classie.remove( body, 'chat-window-open' );
        else
        {
            classie.add( body, 'chat-window-open' );
            // var li = Math.floor((Math.random()* $('.contact-list #on-line-list ul.media-list li').length )+1));
            $('.contact-list #on-line-list ul.media-list li:nth-child(2) a').addClass('status-message');
        }
    });


    // Preventing Dropdown of MEssages to close on inside click
    $('li.dropdown.dropdown-messages a').on('click', function (event) {
        $(this).parent().toggleClass('open');
    });

    $('body').on('click', function (e) {
        if (!$('li.dropdown.dropdown-messages').is(e.target) 
            && $('li.dropdown.dropdown-messages').has(e.target).length === 0 
            && $('.open').has(e.target).length === 0
        ) {
            $('li.dropdown.dropdown-messages').removeClass('open');
        }
    });
    slimScrollSettings.height =  250;
    slimScrollSettings.size =  '10px';
    $('li.dropdown.dropdown-messages .messages-content .tab-pane .messages-container').slimScroll(slimScrollSettings);
})(jQuery, window);