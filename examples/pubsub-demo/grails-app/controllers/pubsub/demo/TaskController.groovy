package pubsub.demo


import static grails.async.web.WebPromises.*

class TaskController {
	static responseFormats = ['json', 'xml']
	
    def index() {
        task {
            sleep 1000
            render "good"
        }
    }

    def error() {
        task {
            sleep(1000)
            throw new RuntimeException("bad")
        }.onError {
            render text:"error occured",status: 500
        }
    }
}
