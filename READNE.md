# host

Host is the *upper monitor* of slave(Android phone on the robot), 
responsible for communication with it. While the *OpMode* is running, slave keeps sending sensor data. 
Once host receives data and finds data changed, 
input data will be posted into a *dataflow* and is delivered at various levels, 
finally come to device(s) output data, sent back to the slave.