var eventSource;

self.addEventListener('message', function(e) {
    switch(e.data) {
        case "END":
          eventSource.close();
          self.close(); // this closes the worker
          break;

        default:
          eventSource = new EventSource(`/progressStatuses/${e.data}`);
          eventSource.addEventListener('message', function( event ) {
                postMessage(event.data);
            });

          eventSource.onerror = function(e) {
              e = e || event, msg = '';
              switch( e.target.readyState ){
                // if reconnecting
                case EventSource.CONNECTING:
                  msg = 'Reconnecting…';
                  break;
                // if error was fatal
                case EventSource.CLOSED:
                  msg = 'Connection failed. Will not retry.';
                  break;
              }
              console.log(msg);
          };
    }
}, false);