import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.neural_network import MLPClassifier
from sklearn.metrics import classification_report, accuracy_score
import joblib

data = pd.read_csv('synthetic_task_dataset.csv')

X = data[['CPU_req', 'Data_size', 'Bandwidth', 'BT', 'BP', 'ECG', 'Delay_thresh']]
y = data['Decision']
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
scaler = StandardScaler()
X_train = scaler.fit_transform(X_train)
X_test = scaler.transform(X_test)
model = MLPClassifier(hidden_layer_sizes=(64, 32), max_iter=500, random_state=42)
model.fit(X_train, y_train)
y_pred = model.predict(X_test)
print("Accuracy:", accuracy_score(y_test, y_pred))
print(classification_report(y_test, y_pred))


joblib.dump(model, 'task_scheduler_model(MLP_Classifier).pkl')
joblib.dump(scaler, 'scaler.pkl')
