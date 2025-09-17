import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report
import xgboost as xgb
import joblib

# Load dataset
data = pd.read_csv('synthetic_task_dataset.csv')
X = data[['CPU_req', 'Data_size', 'Bandwidth', 'BT', 'BP', 'ECG', 'Delay_thresh']]
y = data['Decision']

# Train/test split
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Train XGBoost model
model = xgb.XGBClassifier(use_label_encoder=False, eval_metric='logloss')
model.fit(X_train, y_train)

# Evaluate
y_pred = model.predict(X_test)
print("XGBoost Accuracy:", accuracy_score(y_test, y_pred))
print(classification_report(y_test, y_pred))

# Save model
joblib.dump(model, 'xgboost_task_scheduler.pkl')
joblib.dump(model, 'task_scheduler_model(XGBoost).pkl')