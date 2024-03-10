const { default: axios } = require('axios');
const express = require('express');
const app = express();
const port = 3000;

app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static('../src/main/resources/templates/'));

app.post('/api/v1/submit-code', async (req, res) => {
     const { code, inputs, outputs, method } = req.body;
     console.log(req.body);
     let result = [];

     for (var i = 0; i < inputs.length; i++) {
          try {
               // console.log(inputs[i]);
               // console.log(i);
               const curr_res = await axios.post("http://51.20.51.133/run-code", {
                    code: code,
                    input: inputs[i],
                    language: method
               });
               //   console.log(curr_res.data); 
               result.push(curr_res.data.output);
          } catch (error) {
               console.error("Error occurred during API call:", error.message);
               //   result.push({ error: error.message }); 
          }
     }

     console.log(result);
     let verdicts = [];
     for (var i = 0; i < outputs.length; i++) {
          outputs[i] = outputs[i].trim()
          result[i] = result[i].trim()
          console.log(outputs[i])
          if (outputs[i] !== result[i]) {
               verdicts.push("Failed")
          }
          else {
               verdicts.push("Passed")
          }
     }
     res.status(200).json({ success: true, foutputs: result, verdicts: verdicts });
});


app.post('/api/v1/submit-api', async (req, res) => {
     try {
          const { url, method, request, keys, values } = req.body;
          const headers = {}
          console.log(req.body);
          if (keys.length) {
               for (var i = 0; i < key.length; i++) {
                    headers[keys[i]] = values[i]
               }
          }
          let output = []
          let codes = []
          if (!request.length) {
               request.push("get");
               // console.log(request);
          }
          console.log(request);
          for (var i = 0; i < request.length; i++) {
               try {
                    console.log(request[i]);
                    const requestBody = JSON.parse(request[i]);
                    console.log(requestBody);   
                    const curr_res =await axios({
                         method: method, // Specify the HTTP method
                         url: url, // Specify the URL
                         data: requestBody // Specify the request body
                       })
                    console.log(curr_res.data)
                    //     console.log(curr_res.data)
                    output.push(curr_res.data);
                    codes.push(curr_res.status)
               } catch (error) {
                    console.error("Error occurred during API call:", error.message);
                    //   result.push({ error: error.message }); 
               }
          }

          console.log(output);
          res.json({ outputs: output, codes: codes })
     } catch (e) {

     }
})
app.get('/', (req, res) => {
     res.status(200).json(`Hello from API Code Probe`);
});

app.listen(port, () => {
     console.log(`Server listening to port ${port}`);
});
