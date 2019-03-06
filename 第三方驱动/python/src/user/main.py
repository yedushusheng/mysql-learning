from flask import Flask
from flask import request
from flask import render_template
from flask import redirect
from db import *
app=Flask(__name__)

from wtforms import Form,TextField,PasswordField,validators

class LoginForm(Form):
	username = TextField("username",[validators.Required()])
	password = PasswordField("password",[validators.Required()])


@app.route("/register",methods=['GET','POST'])
def register():
	myForm=LoginForm(request.form)
	if request.method=='POST':
		insert(myForm.username.data,myForm.password.data)
		return redirect("http://www.jikexueyuan.com")
	return render_template('index.html',form=myForm)

@app.route("/login",methods=['GET','POST'])
def login():
	myForm=LoginForm(request.form)
	if request.method =='POST':
		if (isExisted(myForm.username.data,myForm.password.data)):
			return redirect("http://www.jikexueyuan.com")
		else:
			return "Login Failed"
	return render_template('index.html',form=myForm)

if __name__=="__main__":
	app.run(port=8080,debug=True)