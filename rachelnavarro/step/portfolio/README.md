This is Rachel Navarro's portfolio starter project for the 2020 Google STEP Program.
View the portfolio here: https://rachelnavarro-step-2020.uc.r.appspot.com/

By default it contains a barebones web app. To run a local server, execute this
command:

```bash
mvn package appengine:run

UPDATE 7/13/20:
To run the dev server for the portfolio, execute the bash script: ./runPortfolio.sh
This runs a script to substitute all instances of 'MY_API_KEY' with the value of the 
MY_API_KEY environment variable, and subsequently runs the dev server.
