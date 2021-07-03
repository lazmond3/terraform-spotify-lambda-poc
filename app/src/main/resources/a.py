import os
from flask import Flask, send_from_directory, request

app = Flask(__name__)
# app.register_blueprint(app)

@app.route('/index.html')
def index():
  return send_from_directory(".", "index.html")

@app.route('/')
def index_root():
  return send_from_directory(".", "index.html")

@app.route('/index.js')
def indexjs():
  return send_from_directory(".", "index.js")

@app.route("/post", methods=["POST"])
def post():
    print("request data: ", request.data)
    return 'hello world'

if __name__ == '__main__':
#   app.run(host=os.getenv('APP_ADDRESS', 'localhost'), port=8000)
  app.run(host='0.0.0.0', port=8000)
