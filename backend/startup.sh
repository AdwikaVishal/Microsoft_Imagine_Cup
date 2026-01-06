#!/bin/bash

echo "Installing dependencies..."
pip install --upgrade pip
pip install -r requirements.txt

echo "Starting app..."
gunicorn -k uvicorn.workers.UvicornWorker app.main:app --bind=0.0.0.0:$PORT
