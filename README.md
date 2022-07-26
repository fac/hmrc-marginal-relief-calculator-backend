# Marginal Relief Calculator

This service provides an API for calculating the marginal relief and corporation tax liablity, based on the available configuration for financial years. It
also provides a break-down of the calculation to interpreting how the marginal relief and CT liablity was calculated.

## Running in DEV mode

To start the service locally, execute the following command

```$ sbt -jvm-debug DEBUG_PORT run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes ```

To run locally using Service Manager

```sm --start MARGINAL_RELIEF_CALCULATOR_BACKEND```

## REST API Details

### Calculate

Calculates the marginal relief, based on the financial year config and the user parameters.

**Method:** `GET`

**Path:** `/marginal-relief-calculator-backend/calculate`

**Query Params**

|Name|Type|Description|Required|Format|Example Value|
|----|----|-----------|--------|------|-------------|
|accountingPeriodStart|Date|The accounting period start date|Yes|YYYY-MM-DD|2023-01-01|
|accountingPeriodEnd|Date|The accounting period end date|Yes|YYYY-MM-DD|2023-01-01|
|profit|Integer|The total taxable profit|Yes||100000|
|exemptDistributions|Integer|Exempt Distributions|No||10000|
|associatedCompanies|Integer|Number of associated companies|No||1|
|associatedCompaniesFY1|Integer|Number of associated companies for financial year 1, when accounting period spans 2 financial years|No||1|
|associatedCompaniesFY2|Integer|Number of associated companies for financial year 2, when accounting period spans 2 financial years|No||1|

**Responses**

|Status|Code|Response Body|Field Path|Field Message|
|------|----|-------------|----------|-------------|
|200| OK| Marginal relief calculation result as JSON| | |

When successful, the result can either be calculations for a single year or two years (when accounting period spans multiple years)

*Single Result*

```json
 {
     "type": "SingleResult",
     "details": {
        "type":"MarginalRate",
        "taxRateBeforeMR":25,
        "corporationTaxBeforeMR":15000,
        "adjustedDistributions":0,
        "taxRate":20.25,
        "year":2023,
        "adjustedUpperThreshold":250000,
        "marginalRelief":2850,
        "adjustedLowerThreshold":50000,
        "corporationTax":12150,
        "adjustedProfit":60000
      }
 }
```
 *Dual Result*

```json
  {
      "type": "DualResult",
      "year1": {
          "type": "FlatRate",
          "year": 2022,
          "corporationTax": 4684.93,
          "taxRate": 19,
          "adjustedProfit": 24657.53
      },
      "year2": {
          "type": "MarginalRate",
          "taxRateBeforeMR": 25,
          "corporationTaxBeforeMR": 18835.62,
          "adjustedDistributions": 0,
          "taxRate": 22.75,
          "year": 2023,
          "adjustedUpperThreshold": 188356.16,
          "marginalRelief": 1695.21,
          "adjustedLowerThreshold": 37671.23,
          "corporationTax": 17140.41,
          "adjustedProfit": 75342.47
      }
  }
```

### Required Parameters - Associated Companies

Returns the associated companies parameter requirements, given the accounting period, profit and exempt distributions

**Method:** `GET`

**Path:** `/marginal-relief-calculator-backend/ask-params/associated-companies`

**Query Params**

|Name|Type|Description|Required|Format|Example Value|
|----|----|-----------|--------|------|-------------|
|accountingPeriodStart|Date|The accounting period start date|Yes|YYYY-MM-DD|2023-01-01|
|accountingPeriodEnd|Date|The accounting period end date|Yes|YYYY-MM-DD|2023-01-01|
|profit|Integer|The total taxable profit|Yes||100000|
|exemptDistributions|Integer|Exempt Distributions|No||10000|

**Responses**

|Status|Code|Response Body|Field Path|Field Message|
|------|----|-------------|----------|-------------|
|200| OK| AssociatedCompaniesParameter as JSON| | |

When successful, the result can either be DontAsk, AskFull, AskOnePart or AskBothParts results. When the requirement is for one period (AskOnePart or AskFull type), the calculate request expects the associated companies
value via the associatedCompanies query param. When the requirement is for two notional periods (AskBothParts type), the calculate request expected associated companies via associatedCompaniesFY1
and associatedCompaniesFY2 parameters.

*DontAsk result*

```json
  {
    "type": "DontAsk"
  }
```

*AskFull result*

```json
 {
     "type": "AskFull"
 }
```

*AskOnePart result*

```json
 {
     "type": "AskOnePart",
     "period": {
        "start": "2020-01-01",
        "end": "2020-12-31"
     }
 }
```
*AskBothParts result*

```json
  {
      "type": "AskBothParts",
      "period1": {
         "start": "2020-01-01",
         "end": "2020-03-31"
      },
      "period2": {
         "start": "2020-04-01",
         "end": "2020-12-31"
      }
  }
```

