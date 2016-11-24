
import * as diffusion from 'diffusion';

const jsonDataType = diffusion.datatypes.json();

export class RequestContext {
    private id: number = 0;
    private conversations: { [id: number]: { resolve: (response: any) => void, reject: (error: Error) => void } } = {};

    constructor(private session: any, private path: string) {
        session.messages.listen(path, (message) => {
            let response = jsonDataType.readValue(message.content).get();
            let handler = this.conversations[response.id];
            if (handler) {
                if (response.error) {
                    handler.reject(new Error(response.error));
                }
                else if (response.response) {
                    handler.resolve(response.response);
                }
                else {
                    handler.reject(new Error('Badly formatted response'));
                }
                delete this.conversations[response.id];
            }
            else {
                console.log('Received a response with no outstanding promise', response);
            }
        });
    }

    request(message: any): Promise<any> {
        let requestId = this.id;
        this.id += 1;

        return new Promise((resolve, reject) => {
            message['id'] = requestId;

            this.conversations[requestId] = {
                resolve : resolve,
                reject : reject
            };

            this
                .session
                .messages
                .send(this.path, jsonDataType.from(message))
                .then(
                    () => {
                    },
                    (error) => {
                        reject(error);
                        delete this.conversations[requestId];
                    });
        });
    }
}
