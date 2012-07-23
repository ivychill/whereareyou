SRC_DIR=`pwd`
echo "delete protoc generated .cc .h ..."
(cd $SRC_DIR/proto && rm -rf *.pb.*)
/usr/local/bin/protoc -I=$SRC_DIR --cpp_out=$SRC_DIR $SRC_DIR/proto/trackevent.proto
(cd $SRC_DIR/proto && mv *.pb.cc $SRC_DIR)
echo "protoc done."
