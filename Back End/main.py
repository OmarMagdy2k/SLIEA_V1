from fastapi import FastAPI
from english_model import predict_En
from arabic_model import predict_Ar
from firestore import download_image_from_store

app = FastAPI()

@app.get('/')
async def root():
    return {'example':'this is an example', 'data':0}
    
   

@app.get("/translate_image_En/")
async def translate_image_request(file_name):
    file_path=download_image_from_store(file_name)
    prediction=predict_En.predicted_frame(file_path)
    return {"prediction": prediction}

@app.get("/translate_image_Ar/")
async def translate_image_request(file_name):
    file_path=download_image_from_store(file_name)
    prediction=predict_Ar.predicted_frame(file_path)
    return {"prediction": prediction}





# uvicorn main:app --host 0.0.0.0 --port 8000 --reload