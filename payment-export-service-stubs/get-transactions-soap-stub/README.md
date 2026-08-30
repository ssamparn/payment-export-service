# get-transactions-soap-stub

Mock SOAP service for transaction retrieval per batch.

## What it provides

- SOAP endpoint at `/ws`
- Request root element: `GetTransactionsReq`
- Response root element: `GetTransactionsRpy`
- Response includes `moreResultsAvailable` for pagination continuation/stop decisions.
- Request uses `batchId`, `page`, and `pageSize`.
- Transaction records include:
  - `transactionId`
  - `batchId`
  - `batchName`
  - `paymentType` (`CT`, `DD`)
  - `batchStatus` (`CREATED`, `PROCESSING`, `COMPLETED`, `FAILED`)
  - `accountHolderName`
  - `transactionAmount`
  - `currencyCode`
- Pagination contract follows one-based indexing, e.g. `page=1,pageSize=100`, `page=2,pageSize=100`.

## Run

The module is configured for port `7070` in `src/main/resources/application.yaml`.

## Example SOAP Request

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ns="http://payment.export.platform.com/get-transactions-soap">
  <soapenv:Header/>
  <soapenv:Body>
    <ns:GetTransactionsReq>
      <ns:batchId>INT-EUR-0001</ns:batchId>
      <ns:page>1</ns:page>
      <ns:pageSize>100</ns:pageSize>
    </ns:GetTransactionsReq>
  </soapenv:Body>
</soapenv:Envelope>
```

