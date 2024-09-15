import sqlite3
import matplotlib.pyplot as plt
import pandas as pd
import matplotlib.dates as mdates  # for formatting dates in matplotlib

# Connect to the SQLite database
conn = sqlite3.connect('measurements.db')  # Change 'your_database.db' to the path of your database file
cursor = conn.cursor()

# Execute a query to select the data
cursor.execute('SELECT timestamp, value FROM measurements')  # Change 'your_table' to your table name
data = cursor.fetchall()

# Close the connection to the database
cursor.close()
conn.close()

# Separate the data into two lists
timestamps = [row[0] for row in data]
values = [float(row[1]) for row in data]

# Convert timestamps to datetime objects
timestamps = pd.to_datetime(timestamps)

# Plotting the data
plt.figure(figsize=(10, 5))
plt.plot(timestamps, values, linestyle='-')
plt.ylabel('Temperature (F)')
plt.xlabel('Time (UTC)')
plt.grid(True)
# Format the x-axis to properly handle datetime values
plt.gca().xaxis.set_major_locator(mdates.AutoDateLocator())
plt.gca().xaxis.set_major_formatter(mdates.DateFormatter('%Y-%m-%d %H:%M:%S'))
plt.gcf().autofmt_xdate()  # Auto formats the x-axis data to fit better
plt.show()
