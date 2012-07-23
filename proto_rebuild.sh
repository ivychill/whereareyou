SRC_DIR=`pwd`
echo "rebuild protobuf for tracker server (c++)... "
(cd $SRC_DIR/tracker_server && ./proto_rebuild.sh)
echo "rebuild protobuf for tracker client (Android Java)... "
(cd $SRC_DIR/tracker_client_android && ./proto_rebuild.sh)
echo "rebuild protobuf for trackee server (Rails Ruby)... "
(cd $SRC_DIR/trackee_server && ./proto_rebuild.sh)
echo "rebuild done."
