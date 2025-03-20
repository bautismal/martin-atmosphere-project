$(function () {
    "use strict";

    var detect = $('#detect');
    var header = $('#header');
    var content = $('#content');
    var input = $('#input');
    var status = $('#status');
    var myName = false;
    var author = null;
    var logged = false;
    var socket = atmosphere;
    var encoder = new TextEncoder();
    var decoder = new TextDecoder();
    var subSocket = null;
    var binary = true;

    input.keydown(function (e) {
        if (e.keyCode === 13) {
            var msg = $(this).val();

            // First message is always the author's name
            if (author == null) {
                author = msg;
            }

            if (binary) {
                const binaryData = encoder.encode(JSON.stringify({ author: author, message: msg }));
                subSocket.push(binaryData);
            } else {
                subSocket.push(JSON.stringify({ author: author, message: msg }));
            }

            $(this).val('');

            input.attr('disabled', 'disabled');
            if (myName === false) {
                myName = msg;
            }
        }
    });

    function addMessage(author, message, color, datetime) {
        content.append('<p><span style="color:' + color + '">' + author + '</span> @ ' +
            + (datetime.getHours() < 10 ? '0' + datetime.getHours() : datetime.getHours()) + ':'
            + (datetime.getMinutes() < 10 ? '0' + datetime.getMinutes() : datetime.getMinutes())
            + ': ' + message + '</p>');
    }

    function connect() {
        var checkBox = document.getElementById("binaryCheck");
        binary = checkBox.checked;

        // We are now ready to cut the request
        var request = {
            url: 'http://127.0.0.1:8080/chat',
            contentType: "application/json",
            transport: 'websocket',
            headers: {}
        };

        if (binary) {
            request.headers['X-Atmosphere-Binary'] = true;
            request.enableProtocol = false;
            request.trackMessageLength = false;
            request.contentType = 'application/octet-stream';
            request.webSocketBinaryType = 'arraybuffer';
        }

        request.onOpen = function (response) {
            content.html($('<p>', { text: 'Atmosphere connected using ' + response.transport }));
            input.removeAttr('disabled').focus();
            status.text('Choose name:');
        };

        request.onMessage = function (response) {
            var message = response.responseBody;
            if (binary) {
                console.log("binary message : " + message);
                message = decoder.decode(message);
            }
            console.log('onMessage : ', message);
            try {
                var json = JSON.parse(message);
            } catch (e) {
                console.log('This doesn\'t look like a valid JSON: ', message);
                return;
            }

            input.removeAttr('disabled').focus();
            if (!logged && myName) {
                status.text(myName + ': ').css('color', 'blue');
                input.removeAttr('disabled').focus();
                logged = true;
            } else {
                var me = json.author == author;
                var date = typeof (json.time) == 'string' ? parseInt(json.time) : json.time;
                addMessage(json.author, json.message, me ? 'blue' : 'black', new Date());
            }
        };

        request.onClose = function (response) {
            logged = false;
        }

        request.onError = function (response) {
            content.html($('<p>', {
                text: 'Sorry, but there\'s some problem with your '
                    + 'socket or the server is down'
            }));
        };

        subSocket = socket.subscribe(request);
    }

    connect();

    const checkBox = document.getElementById("binaryCheck");
    checkBox.addEventListener("click", function () {
        atmosphere.unsubscribe(subSocket);
        connect();
    });
});