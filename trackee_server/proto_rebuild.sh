SRC_DIR=`pwd`
echo "compile protobuf to ruby..."
(cd $SRC_DIR/proto && rm -rf *.pb.rb)
(cd $SRC_DIR/proto && rprotoc trackevent.proto)
(cd $SRC_DIR/proto && mv *.pb.rb $SRC_DIR/app/controllers)
echo "compile done."
