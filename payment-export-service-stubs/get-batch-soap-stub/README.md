# get-batch-soap-stub

Mock SOAP service for batch metadata retrieval.

## What it provides

- SOAP endpoint at `/ws`
- Request root element: `GetBatchReq`
- Response root element: `GetBatchRpy`
- Response includes `moreResultsAvailable` for pagination continuation/stop decisions.
- Request uses `paymentType`, `page`, `pageSize`, and one or more `account` entries (`iban`, `currencyCode`).
- Batch records include:
  - `batchId` (this is the internal batch id used by the application)
  - `iban`
  - `currencyCode`
  - `paymentType` (`CT`, `DD`)
- Pagination contract follows one-based indexing, e.g. `page=1,pageSize=100`, `page=2,pageSize=100`.

## Run

The module is configured for port `6060` in `src/main/resources/application.yaml`.

## Example SOAP Request

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ns="http://payment.export.platform.com/get-batch-soap">
  <soapenv:Header/>
  <soapenv:Body>
    <ns:GetBatchReq>
      <ns:paymentType>CT</ns:paymentType>
      <ns:page>1</ns:page>
      <ns:pageSize>100</ns:pageSize>
      <ns:account>
        <ns:iban>DE89370400440532013000</ns:iban>
        <ns:currencyCode>EUR</ns:currencyCode>
      </ns:account>
    </ns:GetBatchReq>
  </soapenv:Body>
</soapenv:Envelope>
```