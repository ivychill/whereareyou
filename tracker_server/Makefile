#started with '#' are comments
LIB=-lzmq -lprotobuf -llog4cplus

BIN_PATH=bin
PRG=tracker_srv
OBJ=*.o

CC=g++
CC_FLAG=-Wall

all: tracker_srv

.cpp.o:
	@echo "Compile $(OBJ) begin......"
	$(CC) $(CC_FLAG) -c $<
	@echo "Compile $(OBJ) end......"

.cc.o:
	@echo "Compile $(OBJ) begin......"
	$(CC) $(CC_FLAG) -c $<
	@echo "Compile $(OBJ) end......"

tracker_srv: tracker_server.o trackevent.pb.o my_log.o
	@echo "Link tracker_server begin......"
	$(CC) $(CC_FLAG) -o $(BIN_PATH)/$@ $^ $(LIB)
	@echo "Link tracker_server end......"

clean:
	@echo "Removing linked and compiled files......"
	rm -f $(OBJ)

rebuild:
	@echo "run protoc to compile protobuf......"
	./proto_rebuild.sh
	make clean all

.phony: clean
