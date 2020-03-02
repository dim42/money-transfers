Simple RESTful API for money transfers between internal accounts.

Build (JDK13 is required):  
./gradlew build

Start the server:  
./gradlew run

Create new account:  
curl -H "Content-Type: application/json" -X POST -d '{"request_id":"<some_unique_UUID>"}' "http://localhost:8081/api/1.0/account"

Retrieve account by ID:  
curl "http://localhost:8081/api/1.0/account/<account_id_in_UUID_format>"

Create payment for account (increase its balance, assume there is no account to pay from, only receiver account):  
curl -H "Content-Type: application/json" -X POST -d '{"request_id":"\<UUID\>", "target_acc_id":"\<UUID\>", "amount":21.42}' "http://localhost:8081/api/1.0/payment"

Process transfer between two accounts:
curl -H "Content-Type: application/json" -X POST -d '{"request_id":"\<UUID\>", "source_acc_id":"\<UUID\>", "target_acc_id":"\<UUID\>", "amount":21.42}' "http://localhost:8081/api/1.0/transfer"

Retrieve transaction by ID (after payment or transfer):  
curl "http://localhost:8081/api/1.0/transaction/<transaction_id>"  

No Currency is used for the sake of simplicity.  
Synchronization safety is provided in RequestHandler using ordered locks (getLockWithTimeout()).  
Vertx LocalMap is used as in-memory datastore (values should implement Shareable).  
There is no Unit tests, as all logic is tested via the integration test.
