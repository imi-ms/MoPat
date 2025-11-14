
import json
import os


def get_config():
    with open('secret.json') as f:
        config = json.load(f)
    return config