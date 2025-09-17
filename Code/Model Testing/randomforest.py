from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report
import joblib
import pandas as pd

# Load dataset
data = pd.read_csv('synthetic_task_dataset.csv')
X = data[['CPU_req', 'Data_size', 'Bandwidth', 'BT', 'BP', 'ECG', 'Delay_thresh']]
y = data['Decision']

# Split
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Train Random Forest
rf_model = RandomForestClassifier(n_estimators=100, random_state=42)
rf_model.fit(X_train, y_train)

# Evaluate
y_pred = rf_model.predict(X_test)
print("Random Forest Accuracy:", accuracy_score(y_test, y_pred))
print(classification_report(y_test, y_pred))

# Save model
joblib.dump(rf_model, 'random_forest_task_scheduler.pkl')
joblib.dump(rf_model, 'task_scheduler_model(RandomForest).pkl')