#started with '#' are comments
LIB=-lzmq -lprotobuf

BIN_PATH=bin
PRG=tracker_server
OBJ=*.o

CC=g++
CC_FLAG=-Wall

all: tracker_server

.cpp.o:
	@echo "Compile $(OBJ) begin......"
	$(CC) $(CC_FLAG) -c $<
	@echo "Compile $(OBJ) end......"

.cc.o:
	@echo "Compile $(OBJ) begin......"
	$(CC) $(CC_FLAG) -c $<
	@echo "Compile $(OBJ) end......"

tracker_server: tracker_server.o trackevent.pb.o
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
