// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: deephaven/proto/inputtable.proto

#include "deephaven/proto/inputtable.pb.h"

#include <algorithm>

#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/extension_set.h>
#include <google/protobuf/wire_format_lite.h>
#include <google/protobuf/descriptor.h>
#include <google/protobuf/generated_message_reflection.h>
#include <google/protobuf/reflection_ops.h>
#include <google/protobuf/wire_format.h>
// @@protoc_insertion_point(includes)
#include <google/protobuf/port_def.inc>

PROTOBUF_PRAGMA_INIT_SEG

namespace _pb = ::PROTOBUF_NAMESPACE_ID;
namespace _pbi = _pb::internal;

namespace io {
namespace deephaven {
namespace proto {
namespace backplane {
namespace grpc {
PROTOBUF_CONSTEXPR AddTableRequest::AddTableRequest(
    ::_pbi::ConstantInitialized)
  : input_table_(nullptr)
  , table_to_add_(nullptr){}
struct AddTableRequestDefaultTypeInternal {
  PROTOBUF_CONSTEXPR AddTableRequestDefaultTypeInternal()
      : _instance(::_pbi::ConstantInitialized{}) {}
  ~AddTableRequestDefaultTypeInternal() {}
  union {
    AddTableRequest _instance;
  };
};
PROTOBUF_ATTRIBUTE_NO_DESTROY PROTOBUF_CONSTINIT PROTOBUF_ATTRIBUTE_INIT_PRIORITY1 AddTableRequestDefaultTypeInternal _AddTableRequest_default_instance_;
PROTOBUF_CONSTEXPR AddTableResponse::AddTableResponse(
    ::_pbi::ConstantInitialized){}
struct AddTableResponseDefaultTypeInternal {
  PROTOBUF_CONSTEXPR AddTableResponseDefaultTypeInternal()
      : _instance(::_pbi::ConstantInitialized{}) {}
  ~AddTableResponseDefaultTypeInternal() {}
  union {
    AddTableResponse _instance;
  };
};
PROTOBUF_ATTRIBUTE_NO_DESTROY PROTOBUF_CONSTINIT PROTOBUF_ATTRIBUTE_INIT_PRIORITY1 AddTableResponseDefaultTypeInternal _AddTableResponse_default_instance_;
PROTOBUF_CONSTEXPR DeleteTableRequest::DeleteTableRequest(
    ::_pbi::ConstantInitialized)
  : input_table_(nullptr)
  , table_to_remove_(nullptr){}
struct DeleteTableRequestDefaultTypeInternal {
  PROTOBUF_CONSTEXPR DeleteTableRequestDefaultTypeInternal()
      : _instance(::_pbi::ConstantInitialized{}) {}
  ~DeleteTableRequestDefaultTypeInternal() {}
  union {
    DeleteTableRequest _instance;
  };
};
PROTOBUF_ATTRIBUTE_NO_DESTROY PROTOBUF_CONSTINIT PROTOBUF_ATTRIBUTE_INIT_PRIORITY1 DeleteTableRequestDefaultTypeInternal _DeleteTableRequest_default_instance_;
PROTOBUF_CONSTEXPR DeleteTableResponse::DeleteTableResponse(
    ::_pbi::ConstantInitialized){}
struct DeleteTableResponseDefaultTypeInternal {
  PROTOBUF_CONSTEXPR DeleteTableResponseDefaultTypeInternal()
      : _instance(::_pbi::ConstantInitialized{}) {}
  ~DeleteTableResponseDefaultTypeInternal() {}
  union {
    DeleteTableResponse _instance;
  };
};
PROTOBUF_ATTRIBUTE_NO_DESTROY PROTOBUF_CONSTINIT PROTOBUF_ATTRIBUTE_INIT_PRIORITY1 DeleteTableResponseDefaultTypeInternal _DeleteTableResponse_default_instance_;
}  // namespace grpc
}  // namespace backplane
}  // namespace proto
}  // namespace deephaven
}  // namespace io
static ::_pb::Metadata file_level_metadata_deephaven_2fproto_2finputtable_2eproto[4];
static constexpr ::_pb::EnumDescriptor const** file_level_enum_descriptors_deephaven_2fproto_2finputtable_2eproto = nullptr;
static constexpr ::_pb::ServiceDescriptor const** file_level_service_descriptors_deephaven_2fproto_2finputtable_2eproto = nullptr;

const uint32_t TableStruct_deephaven_2fproto_2finputtable_2eproto::offsets[] PROTOBUF_SECTION_VARIABLE(protodesc_cold) = {
  ~0u,  // no _has_bits_
  PROTOBUF_FIELD_OFFSET(::io::deephaven::proto::backplane::grpc::AddTableRequest, _internal_metadata_),
  ~0u,  // no _extensions_
  ~0u,  // no _oneof_case_
  ~0u,  // no _weak_field_map_
  ~0u,  // no _inlined_string_donated_
  PROTOBUF_FIELD_OFFSET(::io::deephaven::proto::backplane::grpc::AddTableRequest, input_table_),
  PROTOBUF_FIELD_OFFSET(::io::deephaven::proto::backplane::grpc::AddTableRequest, table_to_add_),
  ~0u,  // no _has_bits_
  PROTOBUF_FIELD_OFFSET(::io::deephaven::proto::backplane::grpc::AddTableResponse, _internal_metadata_),
  ~0u,  // no _extensions_
  ~0u,  // no _oneof_case_
  ~0u,  // no _weak_field_map_
  ~0u,  // no _inlined_string_donated_
  ~0u,  // no _has_bits_
  PROTOBUF_FIELD_OFFSET(::io::deephaven::proto::backplane::grpc::DeleteTableRequest, _internal_metadata_),
  ~0u,  // no _extensions_
  ~0u,  // no _oneof_case_
  ~0u,  // no _weak_field_map_
  ~0u,  // no _inlined_string_donated_
  PROTOBUF_FIELD_OFFSET(::io::deephaven::proto::backplane::grpc::DeleteTableRequest, input_table_),
  PROTOBUF_FIELD_OFFSET(::io::deephaven::proto::backplane::grpc::DeleteTableRequest, table_to_remove_),
  ~0u,  // no _has_bits_
  PROTOBUF_FIELD_OFFSET(::io::deephaven::proto::backplane::grpc::DeleteTableResponse, _internal_metadata_),
  ~0u,  // no _extensions_
  ~0u,  // no _oneof_case_
  ~0u,  // no _weak_field_map_
  ~0u,  // no _inlined_string_donated_
};
static const ::_pbi::MigrationSchema schemas[] PROTOBUF_SECTION_VARIABLE(protodesc_cold) = {
  { 0, -1, -1, sizeof(::io::deephaven::proto::backplane::grpc::AddTableRequest)},
  { 8, -1, -1, sizeof(::io::deephaven::proto::backplane::grpc::AddTableResponse)},
  { 14, -1, -1, sizeof(::io::deephaven::proto::backplane::grpc::DeleteTableRequest)},
  { 22, -1, -1, sizeof(::io::deephaven::proto::backplane::grpc::DeleteTableResponse)},
};

static const ::_pb::Message* const file_default_instances[] = {
  &::io::deephaven::proto::backplane::grpc::_AddTableRequest_default_instance_._instance,
  &::io::deephaven::proto::backplane::grpc::_AddTableResponse_default_instance_._instance,
  &::io::deephaven::proto::backplane::grpc::_DeleteTableRequest_default_instance_._instance,
  &::io::deephaven::proto::backplane::grpc::_DeleteTableResponse_default_instance_._instance,
};

const char descriptor_table_protodef_deephaven_2fproto_2finputtable_2eproto[] PROTOBUF_SECTION_VARIABLE(protodesc_cold) =
  "\n deephaven/proto/inputtable.proto\022!io.d"
  "eephaven.proto.backplane.grpc\032\034deephaven"
  "/proto/ticket.proto\"\222\001\n\017AddTableRequest\022"
  ">\n\013input_table\030\001 \001(\0132).io.deephaven.prot"
  "o.backplane.grpc.Ticket\022\?\n\014table_to_add\030"
  "\002 \001(\0132).io.deephaven.proto.backplane.grp"
  "c.Ticket\"\022\n\020AddTableResponse\"\230\001\n\022DeleteT"
  "ableRequest\022>\n\013input_table\030\001 \001(\0132).io.de"
  "ephaven.proto.backplane.grpc.Ticket\022B\n\017t"
  "able_to_remove\030\002 \001(\0132).io.deephaven.prot"
  "o.backplane.grpc.Ticket\"\025\n\023DeleteTableRe"
  "sponse2\246\002\n\021InputTableService\022\201\001\n\024AddTabl"
  "eToInputTable\0222.io.deephaven.proto.backp"
  "lane.grpc.AddTableRequest\0323.io.deephaven"
  ".proto.backplane.grpc.AddTableResponse\"\000"
  "\022\214\001\n\031DeleteTableFromInputTable\0225.io.deep"
  "haven.proto.backplane.grpc.DeleteTableRe"
  "quest\0326.io.deephaven.proto.backplane.grp"
  "c.DeleteTableResponse\"\000BFH\001P\001Z@github.co"
  "m/deephaven/deephaven-core/go/internal/p"
  "roto/inputtableb\006proto3"
  ;
static const ::_pbi::DescriptorTable* const descriptor_table_deephaven_2fproto_2finputtable_2eproto_deps[1] = {
  &::descriptor_table_deephaven_2fproto_2fticket_2eproto,
};
static ::_pbi::once_flag descriptor_table_deephaven_2fproto_2finputtable_2eproto_once;
const ::_pbi::DescriptorTable descriptor_table_deephaven_2fproto_2finputtable_2eproto = {
    false, false, 823, descriptor_table_protodef_deephaven_2fproto_2finputtable_2eproto,
    "deephaven/proto/inputtable.proto",
    &descriptor_table_deephaven_2fproto_2finputtable_2eproto_once, descriptor_table_deephaven_2fproto_2finputtable_2eproto_deps, 1, 4,
    schemas, file_default_instances, TableStruct_deephaven_2fproto_2finputtable_2eproto::offsets,
    file_level_metadata_deephaven_2fproto_2finputtable_2eproto, file_level_enum_descriptors_deephaven_2fproto_2finputtable_2eproto,
    file_level_service_descriptors_deephaven_2fproto_2finputtable_2eproto,
};
PROTOBUF_ATTRIBUTE_WEAK const ::_pbi::DescriptorTable* descriptor_table_deephaven_2fproto_2finputtable_2eproto_getter() {
  return &descriptor_table_deephaven_2fproto_2finputtable_2eproto;
}

// Force running AddDescriptors() at dynamic initialization time.
PROTOBUF_ATTRIBUTE_INIT_PRIORITY2 static ::_pbi::AddDescriptorsRunner dynamic_init_dummy_deephaven_2fproto_2finputtable_2eproto(&descriptor_table_deephaven_2fproto_2finputtable_2eproto);
namespace io {
namespace deephaven {
namespace proto {
namespace backplane {
namespace grpc {

// ===================================================================

class AddTableRequest::_Internal {
 public:
  static const ::io::deephaven::proto::backplane::grpc::Ticket& input_table(const AddTableRequest* msg);
  static const ::io::deephaven::proto::backplane::grpc::Ticket& table_to_add(const AddTableRequest* msg);
};

const ::io::deephaven::proto::backplane::grpc::Ticket&
AddTableRequest::_Internal::input_table(const AddTableRequest* msg) {
  return *msg->input_table_;
}
const ::io::deephaven::proto::backplane::grpc::Ticket&
AddTableRequest::_Internal::table_to_add(const AddTableRequest* msg) {
  return *msg->table_to_add_;
}
void AddTableRequest::clear_input_table() {
  if (GetArenaForAllocation() == nullptr && input_table_ != nullptr) {
    delete input_table_;
  }
  input_table_ = nullptr;
}
void AddTableRequest::clear_table_to_add() {
  if (GetArenaForAllocation() == nullptr && table_to_add_ != nullptr) {
    delete table_to_add_;
  }
  table_to_add_ = nullptr;
}
AddTableRequest::AddTableRequest(::PROTOBUF_NAMESPACE_ID::Arena* arena,
                         bool is_message_owned)
  : ::PROTOBUF_NAMESPACE_ID::Message(arena, is_message_owned) {
  SharedCtor();
  // @@protoc_insertion_point(arena_constructor:io.deephaven.proto.backplane.grpc.AddTableRequest)
}
AddTableRequest::AddTableRequest(const AddTableRequest& from)
  : ::PROTOBUF_NAMESPACE_ID::Message() {
  _internal_metadata_.MergeFrom<::PROTOBUF_NAMESPACE_ID::UnknownFieldSet>(from._internal_metadata_);
  if (from._internal_has_input_table()) {
    input_table_ = new ::io::deephaven::proto::backplane::grpc::Ticket(*from.input_table_);
  } else {
    input_table_ = nullptr;
  }
  if (from._internal_has_table_to_add()) {
    table_to_add_ = new ::io::deephaven::proto::backplane::grpc::Ticket(*from.table_to_add_);
  } else {
    table_to_add_ = nullptr;
  }
  // @@protoc_insertion_point(copy_constructor:io.deephaven.proto.backplane.grpc.AddTableRequest)
}

inline void AddTableRequest::SharedCtor() {
::memset(reinterpret_cast<char*>(this) + static_cast<size_t>(
    reinterpret_cast<char*>(&input_table_) - reinterpret_cast<char*>(this)),
    0, static_cast<size_t>(reinterpret_cast<char*>(&table_to_add_) -
    reinterpret_cast<char*>(&input_table_)) + sizeof(table_to_add_));
}

AddTableRequest::~AddTableRequest() {
  // @@protoc_insertion_point(destructor:io.deephaven.proto.backplane.grpc.AddTableRequest)
  if (auto *arena = _internal_metadata_.DeleteReturnArena<::PROTOBUF_NAMESPACE_ID::UnknownFieldSet>()) {
  (void)arena;
    return;
  }
  SharedDtor();
}

inline void AddTableRequest::SharedDtor() {
  GOOGLE_DCHECK(GetArenaForAllocation() == nullptr);
  if (this != internal_default_instance()) delete input_table_;
  if (this != internal_default_instance()) delete table_to_add_;
}

void AddTableRequest::SetCachedSize(int size) const {
  _cached_size_.Set(size);
}

void AddTableRequest::Clear() {
// @@protoc_insertion_point(message_clear_start:io.deephaven.proto.backplane.grpc.AddTableRequest)
  uint32_t cached_has_bits = 0;
  // Prevent compiler warnings about cached_has_bits being unused
  (void) cached_has_bits;

  if (GetArenaForAllocation() == nullptr && input_table_ != nullptr) {
    delete input_table_;
  }
  input_table_ = nullptr;
  if (GetArenaForAllocation() == nullptr && table_to_add_ != nullptr) {
    delete table_to_add_;
  }
  table_to_add_ = nullptr;
  _internal_metadata_.Clear<::PROTOBUF_NAMESPACE_ID::UnknownFieldSet>();
}

const char* AddTableRequest::_InternalParse(const char* ptr, ::_pbi::ParseContext* ctx) {
#define CHK_(x) if (PROTOBUF_PREDICT_FALSE(!(x))) goto failure
  while (!ctx->Done(&ptr)) {
    uint32_t tag;
    ptr = ::_pbi::ReadTag(ptr, &tag);
    switch (tag >> 3) {
      // .io.deephaven.proto.backplane.grpc.Ticket input_table = 1;
      case 1:
        if (PROTOBUF_PREDICT_TRUE(static_cast<uint8_t>(tag) == 10)) {
          ptr = ctx->ParseMessage(_internal_mutable_input_table(), ptr);
          CHK_(ptr);
        } else
          goto handle_unusual;
        continue;
      // .io.deephaven.proto.backplane.grpc.Ticket table_to_add = 2;
      case 2:
        if (PROTOBUF_PREDICT_TRUE(static_cast<uint8_t>(tag) == 18)) {
          ptr = ctx->ParseMessage(_internal_mutable_table_to_add(), ptr);
          CHK_(ptr);
        } else
          goto handle_unusual;
        continue;
      default:
        goto handle_unusual;
    }  // switch
  handle_unusual:
    if ((tag == 0) || ((tag & 7) == 4)) {
      CHK_(ptr);
      ctx->SetLastTag(tag);
      goto message_done;
    }
    ptr = UnknownFieldParse(
        tag,
        _internal_metadata_.mutable_unknown_fields<::PROTOBUF_NAMESPACE_ID::UnknownFieldSet>(),
        ptr, ctx);
    CHK_(ptr != nullptr);
  }  // while
message_done:
  return ptr;
failure:
  ptr = nullptr;
  goto message_done;
#undef CHK_
}

uint8_t* AddTableRequest::_InternalSerialize(
    uint8_t* target, ::PROTOBUF_NAMESPACE_ID::io::EpsCopyOutputStream* stream) const {
  // @@protoc_insertion_point(serialize_to_array_start:io.deephaven.proto.backplane.grpc.AddTableRequest)
  uint32_t cached_has_bits = 0;
  (void) cached_has_bits;

  // .io.deephaven.proto.backplane.grpc.Ticket input_table = 1;
  if (this->_internal_has_input_table()) {
    target = ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::
      InternalWriteMessage(1, _Internal::input_table(this),
        _Internal::input_table(this).GetCachedSize(), target, stream);
  }

  // .io.deephaven.proto.backplane.grpc.Ticket table_to_add = 2;
  if (this->_internal_has_table_to_add()) {
    target = ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::
      InternalWriteMessage(2, _Internal::table_to_add(this),
        _Internal::table_to_add(this).GetCachedSize(), target, stream);
  }

  if (PROTOBUF_PREDICT_FALSE(_internal_metadata_.have_unknown_fields())) {
    target = ::_pbi::WireFormat::InternalSerializeUnknownFieldsToArray(
        _internal_metadata_.unknown_fields<::PROTOBUF_NAMESPACE_ID::UnknownFieldSet>(::PROTOBUF_NAMESPACE_ID::UnknownFieldSet::default_instance), target, stream);
  }
  // @@protoc_insertion_point(serialize_to_array_end:io.deephaven.proto.backplane.grpc.AddTableRequest)
  return target;
}

size_t AddTableRequest::ByteSizeLong() const {
// @@protoc_insertion_point(message_byte_size_start:io.deephaven.proto.backplane.grpc.AddTableRequest)
  size_t total_size = 0;

  uint32_t cached_has_bits = 0;
  // Prevent compiler warnings about cached_has_bits being unused
  (void) cached_has_bits;

  // .io.deephaven.proto.backplane.grpc.Ticket input_table = 1;
  if (this->_internal_has_input_table()) {
    total_size += 1 +
      ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::MessageSize(
        *input_table_);
  }

  // .io.deephaven.proto.backplane.grpc.Ticket table_to_add = 2;
  if (this->_internal_has_table_to_add()) {
    total_size += 1 +
      ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::MessageSize(
        *table_to_add_);
  }

  return MaybeComputeUnknownFieldsSize(total_size, &_cached_size_);
}

const ::PROTOBUF_NAMESPACE_ID::Message::ClassData AddTableRequest::_class_data_ = {
    ::PROTOBUF_NAMESPACE_ID::Message::CopyWithSizeCheck,
    AddTableRequest::MergeImpl
};
const ::PROTOBUF_NAMESPACE_ID::Message::ClassData*AddTableRequest::GetClassData() const { return &_class_data_; }

void AddTableRequest::MergeImpl(::PROTOBUF_NAMESPACE_ID::Message* to,
                      const ::PROTOBUF_NAMESPACE_ID::Message& from) {
  static_cast<AddTableRequest *>(to)->MergeFrom(
      static_cast<const AddTableRequest &>(from));
}


void AddTableRequest::MergeFrom(const AddTableRequest& from) {
// @@protoc_insertion_point(class_specific_merge_from_start:io.deephaven.proto.backplane.grpc.AddTableRequest)
  GOOGLE_DCHECK_NE(&from, this);
  uint32_t cached_has_bits = 0;
  (void) cached_has_bits;

  if (from._internal_has_input_table()) {
    _internal_mutable_input_table()->::io::deephaven::proto::backplane::grpc::Ticket::MergeFrom(from._internal_input_table());
  }
  if (from._internal_has_table_to_add()) {
    _internal_mutable_table_to_add()->::io::deephaven::proto::backplane::grpc::Ticket::MergeFrom(from._internal_table_to_add());
  }
  _internal_metadata_.MergeFrom<::PROTOBUF_NAMESPACE_ID::UnknownFieldSet>(from._internal_metadata_);
}

void AddTableRequest::CopyFrom(const AddTableRequest& from) {
// @@protoc_insertion_point(class_specific_copy_from_start:io.deephaven.proto.backplane.grpc.AddTableRequest)
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

bool AddTableRequest::IsInitialized() const {
  return true;
}

void AddTableRequest::InternalSwap(AddTableRequest* other) {
  using std::swap;
  _internal_metadata_.InternalSwap(&other->_internal_metadata_);
  ::PROTOBUF_NAMESPACE_ID::internal::memswap<
      PROTOBUF_FIELD_OFFSET(AddTableRequest, table_to_add_)
      + sizeof(AddTableRequest::table_to_add_)
      - PROTOBUF_FIELD_OFFSET(AddTableRequest, input_table_)>(
          reinterpret_cast<char*>(&input_table_),
          reinterpret_cast<char*>(&other->input_table_));
}

::PROTOBUF_NAMESPACE_ID::Metadata AddTableRequest::GetMetadata() const {
  return ::_pbi::AssignDescriptors(
      &descriptor_table_deephaven_2fproto_2finputtable_2eproto_getter, &descriptor_table_deephaven_2fproto_2finputtable_2eproto_once,
      file_level_metadata_deephaven_2fproto_2finputtable_2eproto[0]);
}

// ===================================================================

class AddTableResponse::_Internal {
 public:
};

AddTableResponse::AddTableResponse(::PROTOBUF_NAMESPACE_ID::Arena* arena,
                         bool is_message_owned)
  : ::PROTOBUF_NAMESPACE_ID::internal::ZeroFieldsBase(arena, is_message_owned) {
  // @@protoc_insertion_point(arena_constructor:io.deephaven.proto.backplane.grpc.AddTableResponse)
}
AddTableResponse::AddTableResponse(const AddTableResponse& from)
  : ::PROTOBUF_NAMESPACE_ID::internal::ZeroFieldsBase() {
  _internal_metadata_.MergeFrom<::PROTOBUF_NAMESPACE_ID::UnknownFieldSet>(from._internal_metadata_);
  // @@protoc_insertion_point(copy_constructor:io.deephaven.proto.backplane.grpc.AddTableResponse)
}





const ::PROTOBUF_NAMESPACE_ID::Message::ClassData AddTableResponse::_class_data_ = {
    ::PROTOBUF_NAMESPACE_ID::internal::ZeroFieldsBase::CopyImpl,
    ::PROTOBUF_NAMESPACE_ID::internal::ZeroFieldsBase::MergeImpl,
};
const ::PROTOBUF_NAMESPACE_ID::Message::ClassData*AddTableResponse::GetClassData() const { return &_class_data_; }







::PROTOBUF_NAMESPACE_ID::Metadata AddTableResponse::GetMetadata() const {
  return ::_pbi::AssignDescriptors(
      &descriptor_table_deephaven_2fproto_2finputtable_2eproto_getter, &descriptor_table_deephaven_2fproto_2finputtable_2eproto_once,
      file_level_metadata_deephaven_2fproto_2finputtable_2eproto[1]);
}

// ===================================================================

class DeleteTableRequest::_Internal {
 public:
  static const ::io::deephaven::proto::backplane::grpc::Ticket& input_table(const DeleteTableRequest* msg);
  static const ::io::deephaven::proto::backplane::grpc::Ticket& table_to_remove(const DeleteTableRequest* msg);
};

const ::io::deephaven::proto::backplane::grpc::Ticket&
DeleteTableRequest::_Internal::input_table(const DeleteTableRequest* msg) {
  return *msg->input_table_;
}
const ::io::deephaven::proto::backplane::grpc::Ticket&
DeleteTableRequest::_Internal::table_to_remove(const DeleteTableRequest* msg) {
  return *msg->table_to_remove_;
}
void DeleteTableRequest::clear_input_table() {
  if (GetArenaForAllocation() == nullptr && input_table_ != nullptr) {
    delete input_table_;
  }
  input_table_ = nullptr;
}
void DeleteTableRequest::clear_table_to_remove() {
  if (GetArenaForAllocation() == nullptr && table_to_remove_ != nullptr) {
    delete table_to_remove_;
  }
  table_to_remove_ = nullptr;
}
DeleteTableRequest::DeleteTableRequest(::PROTOBUF_NAMESPACE_ID::Arena* arena,
                         bool is_message_owned)
  : ::PROTOBUF_NAMESPACE_ID::Message(arena, is_message_owned) {
  SharedCtor();
  // @@protoc_insertion_point(arena_constructor:io.deephaven.proto.backplane.grpc.DeleteTableRequest)
}
DeleteTableRequest::DeleteTableRequest(const DeleteTableRequest& from)
  : ::PROTOBUF_NAMESPACE_ID::Message() {
  _internal_metadata_.MergeFrom<::PROTOBUF_NAMESPACE_ID::UnknownFieldSet>(from._internal_metadata_);
  if (from._internal_has_input_table()) {
    input_table_ = new ::io::deephaven::proto::backplane::grpc::Ticket(*from.input_table_);
  } else {
    input_table_ = nullptr;
  }
  if (from._internal_has_table_to_remove()) {
    table_to_remove_ = new ::io::deephaven::proto::backplane::grpc::Ticket(*from.table_to_remove_);
  } else {
    table_to_remove_ = nullptr;
  }
  // @@protoc_insertion_point(copy_constructor:io.deephaven.proto.backplane.grpc.DeleteTableRequest)
}

inline void DeleteTableRequest::SharedCtor() {
::memset(reinterpret_cast<char*>(this) + static_cast<size_t>(
    reinterpret_cast<char*>(&input_table_) - reinterpret_cast<char*>(this)),
    0, static_cast<size_t>(reinterpret_cast<char*>(&table_to_remove_) -
    reinterpret_cast<char*>(&input_table_)) + sizeof(table_to_remove_));
}

DeleteTableRequest::~DeleteTableRequest() {
  // @@protoc_insertion_point(destructor:io.deephaven.proto.backplane.grpc.DeleteTableRequest)
  if (auto *arena = _internal_metadata_.DeleteReturnArena<::PROTOBUF_NAMESPACE_ID::UnknownFieldSet>()) {
  (void)arena;
    return;
  }
  SharedDtor();
}

inline void DeleteTableRequest::SharedDtor() {
  GOOGLE_DCHECK(GetArenaForAllocation() == nullptr);
  if (this != internal_default_instance()) delete input_table_;
  if (this != internal_default_instance()) delete table_to_remove_;
}

void DeleteTableRequest::SetCachedSize(int size) const {
  _cached_size_.Set(size);
}

void DeleteTableRequest::Clear() {
// @@protoc_insertion_point(message_clear_start:io.deephaven.proto.backplane.grpc.DeleteTableRequest)
  uint32_t cached_has_bits = 0;
  // Prevent compiler warnings about cached_has_bits being unused
  (void) cached_has_bits;

  if (GetArenaForAllocation() == nullptr && input_table_ != nullptr) {
    delete input_table_;
  }
  input_table_ = nullptr;
  if (GetArenaForAllocation() == nullptr && table_to_remove_ != nullptr) {
    delete table_to_remove_;
  }
  table_to_remove_ = nullptr;
  _internal_metadata_.Clear<::PROTOBUF_NAMESPACE_ID::UnknownFieldSet>();
}

const char* DeleteTableRequest::_InternalParse(const char* ptr, ::_pbi::ParseContext* ctx) {
#define CHK_(x) if (PROTOBUF_PREDICT_FALSE(!(x))) goto failure
  while (!ctx->Done(&ptr)) {
    uint32_t tag;
    ptr = ::_pbi::ReadTag(ptr, &tag);
    switch (tag >> 3) {
      // .io.deephaven.proto.backplane.grpc.Ticket input_table = 1;
      case 1:
        if (PROTOBUF_PREDICT_TRUE(static_cast<uint8_t>(tag) == 10)) {
          ptr = ctx->ParseMessage(_internal_mutable_input_table(), ptr);
          CHK_(ptr);
        } else
          goto handle_unusual;
        continue;
      // .io.deephaven.proto.backplane.grpc.Ticket table_to_remove = 2;
      case 2:
        if (PROTOBUF_PREDICT_TRUE(static_cast<uint8_t>(tag) == 18)) {
          ptr = ctx->ParseMessage(_internal_mutable_table_to_remove(), ptr);
          CHK_(ptr);
        } else
          goto handle_unusual;
        continue;
      default:
        goto handle_unusual;
    }  // switch
  handle_unusual:
    if ((tag == 0) || ((tag & 7) == 4)) {
      CHK_(ptr);
      ctx->SetLastTag(tag);
      goto message_done;
    }
    ptr = UnknownFieldParse(
        tag,
        _internal_metadata_.mutable_unknown_fields<::PROTOBUF_NAMESPACE_ID::UnknownFieldSet>(),
        ptr, ctx);
    CHK_(ptr != nullptr);
  }  // while
message_done:
  return ptr;
failure:
  ptr = nullptr;
  goto message_done;
#undef CHK_
}

uint8_t* DeleteTableRequest::_InternalSerialize(
    uint8_t* target, ::PROTOBUF_NAMESPACE_ID::io::EpsCopyOutputStream* stream) const {
  // @@protoc_insertion_point(serialize_to_array_start:io.deephaven.proto.backplane.grpc.DeleteTableRequest)
  uint32_t cached_has_bits = 0;
  (void) cached_has_bits;

  // .io.deephaven.proto.backplane.grpc.Ticket input_table = 1;
  if (this->_internal_has_input_table()) {
    target = ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::
      InternalWriteMessage(1, _Internal::input_table(this),
        _Internal::input_table(this).GetCachedSize(), target, stream);
  }

  // .io.deephaven.proto.backplane.grpc.Ticket table_to_remove = 2;
  if (this->_internal_has_table_to_remove()) {
    target = ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::
      InternalWriteMessage(2, _Internal::table_to_remove(this),
        _Internal::table_to_remove(this).GetCachedSize(), target, stream);
  }

  if (PROTOBUF_PREDICT_FALSE(_internal_metadata_.have_unknown_fields())) {
    target = ::_pbi::WireFormat::InternalSerializeUnknownFieldsToArray(
        _internal_metadata_.unknown_fields<::PROTOBUF_NAMESPACE_ID::UnknownFieldSet>(::PROTOBUF_NAMESPACE_ID::UnknownFieldSet::default_instance), target, stream);
  }
  // @@protoc_insertion_point(serialize_to_array_end:io.deephaven.proto.backplane.grpc.DeleteTableRequest)
  return target;
}

size_t DeleteTableRequest::ByteSizeLong() const {
// @@protoc_insertion_point(message_byte_size_start:io.deephaven.proto.backplane.grpc.DeleteTableRequest)
  size_t total_size = 0;

  uint32_t cached_has_bits = 0;
  // Prevent compiler warnings about cached_has_bits being unused
  (void) cached_has_bits;

  // .io.deephaven.proto.backplane.grpc.Ticket input_table = 1;
  if (this->_internal_has_input_table()) {
    total_size += 1 +
      ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::MessageSize(
        *input_table_);
  }

  // .io.deephaven.proto.backplane.grpc.Ticket table_to_remove = 2;
  if (this->_internal_has_table_to_remove()) {
    total_size += 1 +
      ::PROTOBUF_NAMESPACE_ID::internal::WireFormatLite::MessageSize(
        *table_to_remove_);
  }

  return MaybeComputeUnknownFieldsSize(total_size, &_cached_size_);
}

const ::PROTOBUF_NAMESPACE_ID::Message::ClassData DeleteTableRequest::_class_data_ = {
    ::PROTOBUF_NAMESPACE_ID::Message::CopyWithSizeCheck,
    DeleteTableRequest::MergeImpl
};
const ::PROTOBUF_NAMESPACE_ID::Message::ClassData*DeleteTableRequest::GetClassData() const { return &_class_data_; }

void DeleteTableRequest::MergeImpl(::PROTOBUF_NAMESPACE_ID::Message* to,
                      const ::PROTOBUF_NAMESPACE_ID::Message& from) {
  static_cast<DeleteTableRequest *>(to)->MergeFrom(
      static_cast<const DeleteTableRequest &>(from));
}


void DeleteTableRequest::MergeFrom(const DeleteTableRequest& from) {
// @@protoc_insertion_point(class_specific_merge_from_start:io.deephaven.proto.backplane.grpc.DeleteTableRequest)
  GOOGLE_DCHECK_NE(&from, this);
  uint32_t cached_has_bits = 0;
  (void) cached_has_bits;

  if (from._internal_has_input_table()) {
    _internal_mutable_input_table()->::io::deephaven::proto::backplane::grpc::Ticket::MergeFrom(from._internal_input_table());
  }
  if (from._internal_has_table_to_remove()) {
    _internal_mutable_table_to_remove()->::io::deephaven::proto::backplane::grpc::Ticket::MergeFrom(from._internal_table_to_remove());
  }
  _internal_metadata_.MergeFrom<::PROTOBUF_NAMESPACE_ID::UnknownFieldSet>(from._internal_metadata_);
}

void DeleteTableRequest::CopyFrom(const DeleteTableRequest& from) {
// @@protoc_insertion_point(class_specific_copy_from_start:io.deephaven.proto.backplane.grpc.DeleteTableRequest)
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

bool DeleteTableRequest::IsInitialized() const {
  return true;
}

void DeleteTableRequest::InternalSwap(DeleteTableRequest* other) {
  using std::swap;
  _internal_metadata_.InternalSwap(&other->_internal_metadata_);
  ::PROTOBUF_NAMESPACE_ID::internal::memswap<
      PROTOBUF_FIELD_OFFSET(DeleteTableRequest, table_to_remove_)
      + sizeof(DeleteTableRequest::table_to_remove_)
      - PROTOBUF_FIELD_OFFSET(DeleteTableRequest, input_table_)>(
          reinterpret_cast<char*>(&input_table_),
          reinterpret_cast<char*>(&other->input_table_));
}

::PROTOBUF_NAMESPACE_ID::Metadata DeleteTableRequest::GetMetadata() const {
  return ::_pbi::AssignDescriptors(
      &descriptor_table_deephaven_2fproto_2finputtable_2eproto_getter, &descriptor_table_deephaven_2fproto_2finputtable_2eproto_once,
      file_level_metadata_deephaven_2fproto_2finputtable_2eproto[2]);
}

// ===================================================================

class DeleteTableResponse::_Internal {
 public:
};

DeleteTableResponse::DeleteTableResponse(::PROTOBUF_NAMESPACE_ID::Arena* arena,
                         bool is_message_owned)
  : ::PROTOBUF_NAMESPACE_ID::internal::ZeroFieldsBase(arena, is_message_owned) {
  // @@protoc_insertion_point(arena_constructor:io.deephaven.proto.backplane.grpc.DeleteTableResponse)
}
DeleteTableResponse::DeleteTableResponse(const DeleteTableResponse& from)
  : ::PROTOBUF_NAMESPACE_ID::internal::ZeroFieldsBase() {
  _internal_metadata_.MergeFrom<::PROTOBUF_NAMESPACE_ID::UnknownFieldSet>(from._internal_metadata_);
  // @@protoc_insertion_point(copy_constructor:io.deephaven.proto.backplane.grpc.DeleteTableResponse)
}





const ::PROTOBUF_NAMESPACE_ID::Message::ClassData DeleteTableResponse::_class_data_ = {
    ::PROTOBUF_NAMESPACE_ID::internal::ZeroFieldsBase::CopyImpl,
    ::PROTOBUF_NAMESPACE_ID::internal::ZeroFieldsBase::MergeImpl,
};
const ::PROTOBUF_NAMESPACE_ID::Message::ClassData*DeleteTableResponse::GetClassData() const { return &_class_data_; }







::PROTOBUF_NAMESPACE_ID::Metadata DeleteTableResponse::GetMetadata() const {
  return ::_pbi::AssignDescriptors(
      &descriptor_table_deephaven_2fproto_2finputtable_2eproto_getter, &descriptor_table_deephaven_2fproto_2finputtable_2eproto_once,
      file_level_metadata_deephaven_2fproto_2finputtable_2eproto[3]);
}

// @@protoc_insertion_point(namespace_scope)
}  // namespace grpc
}  // namespace backplane
}  // namespace proto
}  // namespace deephaven
}  // namespace io
PROTOBUF_NAMESPACE_OPEN
template<> PROTOBUF_NOINLINE ::io::deephaven::proto::backplane::grpc::AddTableRequest*
Arena::CreateMaybeMessage< ::io::deephaven::proto::backplane::grpc::AddTableRequest >(Arena* arena) {
  return Arena::CreateMessageInternal< ::io::deephaven::proto::backplane::grpc::AddTableRequest >(arena);
}
template<> PROTOBUF_NOINLINE ::io::deephaven::proto::backplane::grpc::AddTableResponse*
Arena::CreateMaybeMessage< ::io::deephaven::proto::backplane::grpc::AddTableResponse >(Arena* arena) {
  return Arena::CreateMessageInternal< ::io::deephaven::proto::backplane::grpc::AddTableResponse >(arena);
}
template<> PROTOBUF_NOINLINE ::io::deephaven::proto::backplane::grpc::DeleteTableRequest*
Arena::CreateMaybeMessage< ::io::deephaven::proto::backplane::grpc::DeleteTableRequest >(Arena* arena) {
  return Arena::CreateMessageInternal< ::io::deephaven::proto::backplane::grpc::DeleteTableRequest >(arena);
}
template<> PROTOBUF_NOINLINE ::io::deephaven::proto::backplane::grpc::DeleteTableResponse*
Arena::CreateMaybeMessage< ::io::deephaven::proto::backplane::grpc::DeleteTableResponse >(Arena* arena) {
  return Arena::CreateMessageInternal< ::io::deephaven::proto::backplane::grpc::DeleteTableResponse >(arena);
}
PROTOBUF_NAMESPACE_CLOSE

// @@protoc_insertion_point(global_scope)
#include <google/protobuf/port_undef.inc>