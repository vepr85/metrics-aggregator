# Backend Interview Assignment

### Premise
Our senior leadership is asking for performance data for our clients and we need to deliver this to them ASAP.  
The metrics server / api is controlled by another team and we cannot change it.  
Assume the provided api is what is deployed on a remote server, and we cannot access anything besides the public endpoints. 

### Setup
1. Import this project into your IDE of choice
2. Running the `Main` class in the `backend-interview-api` module will launch a server running on `http://localhost:8081`, 
which exposes two endpoints
   1. `/v1/brands`: Returns a json list of brands (customers) with their respective ids and names
   2. `/v1/brands/{brandId}`: Returns the brand for the provided `brandId`
   2. `/v1/metrics`: Returns json object containing metrics data for brands.  The response contains the following fields:
      1. `size`: The total number of items in the response
      2. `brandMetrics`: A `json` array containing objects with the following fields:
         1. `brandId`: The `brandId` the data corresponds to
         2. `metrics`: A list of metrics and associated counts for each metric
         3. `dateTime`: The corresponding dateTime of the counts, rounded to the minute
   3. `/v1/metrics?{brandId}`: Same as `/v1/metrics`, with data filtered to only show for the specified brandId (if it exists)
   4. `/v1/metricTypes`: The list of possible metric types in the system
        
   An example response is the following:
    ```json
    {
      "size": 3,
      "brandMetrics":[
        {
          "brandId": 71,
          "metrics": [
            {
              "metric": "impression",
              "count": 402702
            },
            {
              "metric": "click",
              "count": 3
            }
          ],
          "dateTime": "2018-09-12T16:09:00Z"
        },
        {
          "brandId": 78,
          "metrics": [
            {
              "metric": "impression",
              "count": 296815
            }
          ],
          "dateTime": "2018-09-11T19:18:00Z"
        },
        {
          "brandId": 89,
          "metrics": [
            {
              "metric": "impression",
              "count": 839274
            },
            {
              "metric": "click",
              "count": 8
            },
            {
              "metric": "interaction",
              "count": 24
            }
          ],
          "dateTime": "2018-09-12T15:24:00Z"
        }
      ]
    }
    ```

### Task
Your task is to create a spring boot web application (in it's own module, separate from `backend-interview-api`), 
which aggregates metric data and returns it to the user.  Your application has the following requirements:
  1. You cannot change any code in the `backend-interview-api` module
  2. Collect as many metrics as possible from the backend api endpoint `http://localhost:8081/v1/metrics` as you can in 
  a 300ms window.  Any responses not received within 300ms can be discarded
  
  2. Your application must expose the following views of the data to the user:
     1. Total sum of each metric collected, grouped by brand, sorted by total impressions descending, i.e.
     ```json
        {
           "brandId": 65,
           "brandName": "Brand A",
           "metrics": [
             {"impression": 10, "click": 1, "interaction": 0}
           ]
        }
     ```
     2. Total sum of each metric collected, grouped by datetime (rounded to the hour) and brand, sorted by datetime, then brand name, ascending, i.e.
     ```json
        {
           "brandId": 65,
           "brandName": "Brand A",
           "dateTime": "2018-09-11T19:00:00Z",
           "metrics": [
             {"impression": 10, "click": 1, "interaction": 0}
           ]
        }
     ```
     3. Total sum of the metric counts collected, grouped by metric, i.e.
     ```json
        {
           "impression": 65,
           "click": 7,
           "interaction": 17,
        }
     ```
     4. Total sum of the metric counts, grouped by datetime (rounded to the hour), sorted by datetime ascending, i.e.
     ```json
        {
           "dateTime": "2018-09-11T19:00:00Z",
           "metrics": [
             {"impression": 10, "click": 1, "interaction": 0}
           ]
        }
     ```
  3. The above example response formats aren't required to be used. You can format your response in any format you see fit 
  
  4. In responses grouped by brand, if a brand did not have any data collected for it you must include it with 0s for 
  each metric type.  Similarly, if data was collected for a brand but counts are missing for one or more metric types, 
  0s must be specified for the counts
   
  5. Our senior leadership is not familiar with the brand ids, so **all** responses which include the `brandId` must 
  also include the corresponding `brandName`
  
  6. You can use any available open source dependencies you'd like
  
  7. Once completed, zip the entire project and email back to us
  
